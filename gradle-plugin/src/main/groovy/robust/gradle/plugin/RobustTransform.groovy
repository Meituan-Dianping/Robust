package robust.gradle.plugin

import com.android.build.api.transform.*
import com.android.build.gradle.internal.pipeline.TransformManager
import com.meituan.robust.Constants
import javassist.*
import javassist.bytecode.AccessFlag
import javassist.expr.*
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.logging.Logger

import java.util.concurrent.ForkJoinPool
import java.util.concurrent.atomic.AtomicInteger
import java.util.jar.JarOutputStream
import java.util.zip.GZIPOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
/**
 * Created by mivanzhang on 16/11/3.
 *
 * insert code
 *
 */

class RobustTransform extends Transform implements Plugin<Project> {
    Project project
    static Logger logger
    private static List<String> hotfixPackageList = new ArrayList<>();
    private static List<String> hotfixMethodList = new ArrayList<>();
    private static List<String> exceptPackageList = new ArrayList<>();
    private static List<String> exceptMethodList = new ArrayList<>();
    private static boolean isHotfixMethodLevel = false;
    private static boolean isExceptMethodLevel = false;
//    private static boolean isForceInsert = true;
    private static boolean isForceInsert = false;
    def robust

    @Override
    void apply(Project target) {
        project = target
        robust = new XmlSlurper().parse(new File("${project.projectDir}/${Constants.ROBUST_XML}"))
        logger = project.logger
        initConfig()
        //turnOnDevelopModel 是true的话，则强制执行插入
        if (!isForceInsert) {
            def taskNames = project.gradle.startParameter.taskNames
            def isDebugTask = false;
            for (int index = 0; index < taskNames.size(); ++index) {
                def taskName = taskNames[index]
                logger.debug "input start parameter task is ${taskName}"
                //FIXME: assembleRelease下屏蔽Prepare，这里因为还没有执行Task，没法直接通过当前的BuildType来判断，所以直接分析当前的startParameter中的taskname，
                //另外这里有一个小坑task的名字不能是缩写必须是全称 例如assembleDebug不能是任何形式的缩写输入
                if (taskName.endsWith("Debug") && taskName.contains("Debug")) {
//                    logger.warn " Don't register robust transform for debug model !!! task is：${taskName}"
                    isDebugTask = true
                    break;
                }
            }
            if (!isDebugTask) {
                project.android.registerTransform(this)
                project.afterEvaluate(new RobustApkHashAction())
                logger.quiet "Register robust transform successful !!!"
            }
            if (null != robust.switch.turnOnRobust && !"true".equals(String.valueOf(robust.switch.turnOnRobust))) {
                return;
            }
        } else {
            project.android.registerTransform(this)
            project.afterEvaluate(new RobustApkHashAction())
        }
    }

    def initConfig() {
        hotfixPackageList = new ArrayList<>()
        hotfixMethodList = new ArrayList<>()
        exceptPackageList = new ArrayList<>()
        exceptMethodList = new ArrayList<>()
        isHotfixMethodLevel = false;
        isExceptMethodLevel = false;
        /*对文件进行解析*/
        for (name in robust.packname.name) {
            hotfixPackageList.add(name.text());
        }
        for (name in robust.exceptPackname.name) {
            exceptPackageList.add(name.text());
        }
        for (name in robust.hotfixMethod.name) {
            hotfixMethodList.add(name.text());
        }
        for (name in robust.exceptMethod.name) {
            exceptMethodList.add(name.text());
        }

        if (null != robust.switch.filterMethod && "true".equals(String.valueOf(robust.switch.turnOnHotfixMethod.text()))) {
            isHotfixMethodLevel = true;
        }
        if (null != robust.switch.filterMethod && "true".equals(String.valueOf(robust.switch.turnOnExceptMethod.text()))) {
            isExceptMethodLevel = true;
        }

        if (robust.switch.forceInsert != null && "true".equals(String.valueOf(robust.switch.forceInsert.text())))
            isForceInsert = true
        else
            isForceInsert = false

    }

    @Override
    String getName() {
        return "robust"
    }

    @Override
    Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_CLASS
    }

    @Override
    Set<QualifiedContent.Scope> getScopes() {
        return TransformManager.SCOPE_FULL_PROJECT
    }

    @Override
    boolean isIncremental() {
        return false
    }


    @Override
    void transform(Context context, Collection<TransformInput> inputs, Collection<TransformInput> referencedInputs, TransformOutputProvider outputProvider, boolean isIncremental) throws IOException, TransformException, InterruptedException {
        insertMethodCount.set(0);
        logger.quiet '================robust start================'
        def startTime = System.currentTimeMillis()
        outputProvider.deleteAll()
        File jarFile = outputProvider.getContentLocation("main", getOutputTypes(), getScopes(),
                Format.JAR);
        if(!jarFile.getParentFile().exists()){
            jarFile.getParentFile().mkdirs();
        }
        if(jarFile.exists()){
            jarFile.delete();
        }

        ClassPool classPool = new ClassPool()
        project.android.bootClasspath.each {
//            logger.debug "android.bootClasspath   " + (String) it.absolutePath
            classPool.appendClassPath((String) it.absolutePath)
        }

        def box = ConvertUtils.toCtClasses(inputs, classPool)
        def cost = (System.currentTimeMillis() - startTime) / 1000
//        logger.quiet "check all class cost $cost second, class count: ${box.size()}"
        insertRobustCode(box, jarFile)
        writeMap2File(methodMap, Constants.METHOD_MAP_OUT_PATH)
        cost = (System.currentTimeMillis() - startTime) / 1000
        logger.quiet "robust cost $cost second"
        logger.quiet '================robust   end================'
    }

    private void writeMap2File(Map map, String path) {
        File file = new File(project.buildDir.path + path);
        if (!file.exists() && (!file.parentFile.mkdirs() || !file.createNewFile())) {
//            logger.error(path + " file create error!!")
        }
        FileOutputStream fileOut = new FileOutputStream(file);

        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        ObjectOutputStream objOut = new ObjectOutputStream(byteOut);
        objOut.writeObject(map)
        //gzip压缩
        GZIPOutputStream gzip = new GZIPOutputStream(fileOut);
        gzip.write(byteOut.toByteArray())

        objOut.close();
        gzip.flush();
        gzip.close();
        fileOut.flush()
        fileOut.close()

    }

    def isNeedInsertClass(String className) {
        //这样子可以在需要埋点的剔除指定的类
        for (String exceptName : exceptPackageList) {
            if (className.startsWith(exceptName)) {
                return false;
            }
        }
        for (String name : hotfixPackageList) {
            if (className.startsWith(name)) {
                return true;
            }
        }
        return false;
    }
    static AtomicInteger insertMethodCount = new AtomicInteger(0);

    def insertRobustCode(List<CtClass> box, File jarFile) {
        ZipOutputStream outStream=new JarOutputStream(new FileOutputStream(jarFile));
        new ForkJoinPool().submit {
            box.each { ctClass ->
                if (isNeedInsertClass(ctClass.getName())) {
                    ctClass.setModifiers(AccessFlag.setPublic(ctClass.getModifiers()))
                    boolean addIncrementalChange = false;
                    ctClass.declaredBehaviors.findAll {
                        if (ctClass.isInterface() || ctClass.declaredMethods.length < 1) {
                            return false;
                        }
                        if (!addIncrementalChange) {
                            addIncrementalChange = true;
                            ClassPool classPool = it.declaringClass.classPool
                            CtClass type = classPool.getOrNull(Constants.INTERFACE_NAME);
                            CtField ctField = new CtField(type, Constants.INSERT_FIELD_NAME, ctClass);
                            ctField.setModifiers(AccessFlag.PUBLIC | AccessFlag.STATIC)
                            ctClass.addField(ctField)
//                            logger.debug "ctClass: " + ctClass.getName();
                        }

                        if (it.getMethodInfo().isStaticInitializer()) {
                            return false
                        }

                        // synthetic 方法暂时不aop 比如AsyncTask 会生成一些同名 synthetic方法,对synthetic 以及private的方法也插入的代码，主要是针对lambda表达式
                        if ((it.getModifiers() & AccessFlag.SYNTHETIC) != 0 && !AccessFlag.isPrivate(it.getModifiers())) {
                            return false
                        }
                        if (it.getMethodInfo().isConstructor()) {
                            return false
                        }

                        if ((it.getModifiers() & AccessFlag.ABSTRACT) != 0) {
                            return false
                        }
                        if ((it.getModifiers() & AccessFlag.NATIVE) != 0) {
                            return false
                        }
                        if ((it.getModifiers() & AccessFlag.INTERFACE) != 0) {
                            return false
                        }

                        if (it.getMethodInfo().isMethod()) {
                            if (AccessFlag.isPackage(it.modifiers)) {
                                it.setModifiers(AccessFlag.setPublic(it.modifiers))
                            }
                            boolean flag = modifyMethodCodeFilter(it)
                            if (!flag) {
                                return false
                            }
                        }
                        //方法过滤
                        if (isExceptMethodLevel && exceptMethodList != null) {
                            for (String exceptMethod : exceptMethodList) {
                                if (it.name.matches(exceptMethod)) {
                                    return false
                                }
                            }
                        }

                        if (isHotfixMethodLevel && hotfixMethodList != null) {
                            for (String name : hotfixMethodList) {
                                if (it.name.matches(name)) {
                                    return true
                                }
                            }
                        }
                        return !isHotfixMethodLevel
                    }.each { ctBehavior ->
                        // methodMap must be put here
                        methodMap.put(ctBehavior.longName, insertMethodCount.incrementAndGet());
                        try {
                        if (ctBehavior.getMethodInfo().isMethod()) {
                                boolean isStatic = ctBehavior.getModifiers() & AccessFlag.STATIC;
                                CtClass returnType = ctBehavior.getReturnType0();
                                String returnTypeString = returnType.getName();
                                def body = "Object argThis = null;";
                                if (!isStatic) {
                                    body += "argThis = \$0;"
                                }
                                String parametersClassType=getParametersClassType(ctBehavior as CtMethod)
//                                body += "   if (com.meituan.robust.PatchProxy.isSupport(\$args, argThis, ${Constants.INSERT_FIELD_NAME}, $isStatic, " + methodMap.get(ctBehavior.longName) + ",${parametersClassType},${returnTypeString}.class)) {"
                                body += "   if (com.meituan.robust.PatchProxy.isSupport(new com.meituan.robust.Arguments(\$args, argThis, ${Constants.INSERT_FIELD_NAME}, $isStatic, " + methodMap.get(ctBehavior.longName) + ",${parametersClassType},${returnTypeString}.class))) {"
                                body += getReturnStatement(returnTypeString, isStatic, methodMap.get(ctBehavior.longName),parametersClassType,returnTypeString+".class");
                                body += "   }"
//                                println("before insert body "+body);
                                ctBehavior.insertBefore(body);
                            }
                        } catch (Throwable t ) {
                            t.printStackTrace();
                            logger.error "ctClass: " + ctClass.getName() + " error: " + t.getMessage();
                        }
                    }
                    }
                zipFile(ctClass.toBytecode(),outStream,ctClass.name.replaceAll("\\.","/")+".class");
            }
        }.get()
        outStream.close();
        logger.debug "robust insertMethodCount: " + insertMethodCount.get()
    }

    def String getParametersClassType(CtMethod method){
        if(method.parameterTypes.length==0){
            return " null ";
        }
       StringBuilder parameterType=new StringBuilder();
        parameterType.append("new Class[]{")
        for(CtClass paramterClass:method.getParameterTypes()){
            parameterType.append(paramterClass.name+".class,")
        }
        //remove last ','
        if(','==parameterType.charAt(parameterType.length()-1))
            parameterType.deleteCharAt(parameterType.length()-1)
        parameterType.append("}")
        return parameterType.toString();
    }

    def void zipFile(byte[] classBytesArray, ZipOutputStream zos, String entryName){
        ZipEntry entry = new ZipEntry(entryName);
        zos.putNextEntry(entry);
        zos.write(classBytesArray,0,classBytesArray.length);
        zos.closeEntry()
        zos.flush();
    }


    def HashMap<String, Integer> methodMap = new HashMap();


    /**
     * 判断是否有方法调用
     * @return 是否插桩
     */
    def boolean modifyMethodCodeFilter(CtMethod ctMethod) {

        if (ctMethod == null) {
            logger.debug "print--> ctMethod is null"
            return false
        }
        //判断代码中是否有方法调用
        def isCallMethod = false;
        ctMethod.instrument(new ExprEditor() {

            /**
             * Edits a <tt>new</tt> expression (overridable).
             * The default implementation performs nothing.
             *
             * @param e the <tt>new</tt> expression creating an object.
             */
            public void edit(NewExpr e) throws CannotCompileException { isCallMethod = true; }

            /**
             * Edits an expression for array creation (overridable).
             * The default implementation performs nothing.
             *
             * @param a the <tt>new</tt> expression for creating an array.
             * @throws CannotCompileException
             */
            public void edit(NewArray a) throws CannotCompileException { isCallMethod = true; }

            /**
             * Edits a method call (overridable).
             *
             * The default implementation performs nothing.
             */
            public void edit(MethodCall m) throws CannotCompileException { isCallMethod = true; }

            /**
             * Edits a constructor call (overridable).
             * The constructor call is either
             * <code>super()</code> or <code>this()</code>
             * included in a constructor body.
             *
             * The default implementation performs nothing.
             *
             * @see #edit(NewExpr)
             */
            public void edit(ConstructorCall c) throws CannotCompileException {
                isCallMethod = true;
            }

            /**
             * Edits an instanceof expression (overridable).
             * The default implementation performs nothing.
             */
            public void edit(Instanceof i) throws CannotCompileException { isCallMethod = true; }

            /**
             * Edits an expression for explicit type casting (overridable).
             * The default implementation performs nothing.
             */
            public void edit(Cast c) throws CannotCompileException { isCallMethod = true; }

            /**
             * Edits a catch clause (overridable).
             * The default implementation performs nothing.
             */
            public void edit(Handler h) throws CannotCompileException { isCallMethod = true; }
        })
        return isCallMethod
    }
    /**
     * 根据传入类型判断调用PathProxy的方法
     * @param type 返回类型
     * @param isStatic 是否是静态方法
     * @param methodNumber 方法数
     * @return 返回return语句
     */
    def String getReturnStatement(String type, boolean isStatic, int methodNumber,String parametersClassType,String returnTypeString) {
        switch (type) {
            case Constants.CONSTRUCTOR:
                return "    com.meituan.robust.PatchProxy.accessDispatchVoid(new com.meituan.robust.Arguments(\$args, argThis, changeQuickRedirect, $isStatic, $methodNumber,$parametersClassType,$returnTypeString));  "
            case Constants.LANG_VOID:
                return "    com.meituan.robust.PatchProxy.accessDispatchVoid(new com.meituan.robust.Arguments(\$args, argThis, changeQuickRedirect, $isStatic, $methodNumber,$parametersClassType,$returnTypeString));   return null;"

            case Constants.VOID:
                return "    com.meituan.robust.PatchProxy.accessDispatchVoid(new com.meituan.robust.Arguments(\$args, argThis, changeQuickRedirect, $isStatic, $methodNumber,$parametersClassType,$returnTypeString));   return ;"

            case Constants.LANG_BOOLEAN:
                return "   return ((java.lang.Boolean)com.meituan.robust.PatchProxy.accessDispatch(new com.meituan.robust.Arguments(\$args, argThis, changeQuickRedirect, $isStatic, $methodNumber,$parametersClassType,$returnTypeString)));"
            case Constants.BOOLEAN:
                return "   return ((java.lang.Boolean)com.meituan.robust.PatchProxy.accessDispatch(new com.meituan.robust.Arguments(\$args, argThis, changeQuickRedirect, $isStatic, $methodNumber,$parametersClassType,$returnTypeString))).booleanValue();"

            case Constants.INT:
                return "   return ((java.lang.Integer)com.meituan.robust.PatchProxy.accessDispatch(new com.meituan.robust.Arguments(\$args, argThis, changeQuickRedirect, $isStatic, $methodNumber,$parametersClassType,$returnTypeString))).intValue();"
            case Constants.LANG_INT:
                return "   return ((java.lang.Integer)com.meituan.robust.PatchProxy.accessDispatch(new com.meituan.robust.Arguments(\$args, argThis, changeQuickRedirect, $isStatic, $methodNumber,$parametersClassType,$returnTypeString))); "

            case Constants.LONG:
                return "   return ((java.lang.Long)com.meituan.robust.PatchProxy.accessDispatch(new com.meituan.robust.Arguments(\$args, argThis, changeQuickRedirect, $isStatic, $methodNumber,$parametersClassType,$returnTypeString))).longValue();"
            case Constants.LANG_LONG:
                return "   return ((java.lang.Long)com.meituan.robust.PatchProxy.accessDispatch(new com.meituan.robust.Arguments(\$args, argThis, changeQuickRedirect, $isStatic, $methodNumber,$parametersClassType,$returnTypeString)));"

            case Constants.DOUBLE:
                return "   return ((java.lang.Double)com.meituan.robust.PatchProxy.accessDispatch(new com.meituan.robust.Arguments(\$args, argThis, changeQuickRedirect, $isStatic, $methodNumber,$parametersClassType,$returnTypeString))).doubleValue();"
            case Constants.LANG_DOUBLE:
                return "   return ((java.lang.Double)com.meituan.robust.PatchProxy.accessDispatch(new com.meituan.robust.Arguments(\$args, argThis, changeQuickRedirect, $isStatic, $methodNumber,$parametersClassType,$returnTypeString)));"

            case Constants.FLOAT:
                return "   return ((java.lang.Float)com.meituan.robust.PatchProxy.accessDispatch(new com.meituan.robust.Arguments(\$args, argThis, changeQuickRedirect, $isStatic, $methodNumber,$parametersClassType,$returnTypeString))).floatValue();"
            case Constants.LANG_FLOAT:
                return "   return ((java.lang.Float)com.meituan.robust.PatchProxy.accessDispatch(new com.meituan.robust.Arguments(\$args, argThis, changeQuickRedirect, $isStatic, $methodNumber,$parametersClassType,$returnTypeString)));"

            case Constants.SHORT:
                return "   return ((java.lang.Short)com.meituan.robust.PatchProxy.accessDispatch(new com.meituan.robust.Arguments(\$args, argThis, changeQuickRedirect, $isStatic, $methodNumber,$parametersClassType,$returnTypeString))).shortValue();"
            case Constants.LANG_SHORT:
                return "   return ((java.lang.Short)com.meituan.robust.PatchProxy.accessDispatch(new com.meituan.robust.Arguments(\$args, argThis, changeQuickRedirect, $isStatic, $methodNumber,$parametersClassType,$returnTypeString)));"

            case Constants.BYTE:
                return "   return ((java.lang.Byte)com.meituan.robust.PatchProxy.accessDispatch(new com.meituan.robust.Arguments(\$args, argThis, changeQuickRedirect, $isStatic, $methodNumber,$parametersClassType,$returnTypeString))).byteValue();"
            case Constants.LANG_BYTE:
                return "   return ((java.lang.Byte)com.meituan.robust.PatchProxy.accessDispatch(new com.meituan.robust.Arguments(\$args, argThis, changeQuickRedirect, $isStatic, $methodNumber,$parametersClassType,$returnTypeString)));"
            case Constants.CHAR:
                return "   return ((java.lang.Character)com.meituan.robust.PatchProxy.accessDispatch(new com.meituan.robust.Arguments(\$args, argThis, changeQuickRedirect, $isStatic, $methodNumber,$parametersClassType,$returnTypeString))).charValue();"
            case Constants.LANG_CHARACTER:
                return "   return ((java.lang.Character)com.meituan.robust.PatchProxy.accessDispatch(new com.meituan.robust.Arguments(\$args, argThis, changeQuickRedirect, $isStatic, $methodNumber,$parametersClassType,$returnTypeString)));"
            default:
                return "   return ($type)com.meituan.robust.PatchProxy.accessDispatch(new com.meituan.robust.Arguments(\$args, argThis, changeQuickRedirect, $isStatic, $methodNumber,$parametersClassType,$returnTypeString));"
        }
    }

}