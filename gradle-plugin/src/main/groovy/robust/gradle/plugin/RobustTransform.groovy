package robust.gradle.plugin

import com.android.build.api.transform.*
import com.android.build.gradle.internal.pipeline.TransformManager
import com.meituan.robust.Constants
import javassist.ClassPool
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.logging.Logger
import robust.gradle.plugin.asm.AsmInsertImpl
import robust.gradle.plugin.javaassist.JavaAssistInsertImpl

import java.util.zip.GZIPOutputStream
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
//    private static boolean useASM = false;
    private static boolean useASM = true;
    def robust
    InsertcodeStrategy insertcodeStrategy;

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

        if (null != robust.switch.useAsm && "false".equals(String.valueOf(robust.switch.useAsm.text()))) {
            useASM = false;
        }else {
            //默认使用asm
            useASM = true;
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
            classPool.appendClassPath((String) it.absolutePath)
        }

        def box = ConvertUtils.toCtClasses(inputs, classPool)
        def cost = (System.currentTimeMillis() - startTime) / 1000
//        logger.quiet "check all class cost $cost second, class count: ${box.size()}"
        if(useASM){
            insertcodeStrategy=new AsmInsertImpl(hotfixPackageList,hotfixMethodList,exceptPackageList,exceptMethodList,isHotfixMethodLevel,isExceptMethodLevel);
        }else {
            insertcodeStrategy=new JavaAssistInsertImpl(hotfixPackageList,hotfixMethodList,exceptPackageList,exceptMethodList,isHotfixMethodLevel,isExceptMethodLevel);
        }
        insertcodeStrategy.insertCode(box, jarFile);
        writeMap2File(insertcodeStrategy.methodMap, Constants.METHOD_MAP_OUT_PATH)

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

}