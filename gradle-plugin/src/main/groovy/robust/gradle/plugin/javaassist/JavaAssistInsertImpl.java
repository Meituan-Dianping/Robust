package robust.gradle.plugin.javaassist;

import com.meituan.robust.Constants;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipOutputStream;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtBehavior;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;
import javassist.NotFoundException;
import javassist.bytecode.AccessFlag;
import javassist.expr.Cast;
import javassist.expr.ConstructorCall;
import javassist.expr.ExprEditor;
import javassist.expr.Handler;
import javassist.expr.Instanceof;
import javassist.expr.MethodCall;
import javassist.expr.NewArray;
import javassist.expr.NewExpr;
import robust.gradle.plugin.InsertcodeStrategy;


/**
 * Created by zhangmeng on 2017/5/10.
 * this class do almost the same thing with AsmInsertImpl
 */

public class JavaAssistInsertImpl extends InsertcodeStrategy {

    public JavaAssistInsertImpl(List<String> hotfixPackageList, List<String> hotfixMethodList, List<String> exceptPackageList, List<String> exceptMethodList, boolean isHotfixMethodLevel, boolean isExceptMethodLevel, boolean isForceInsertLambda) {
        super(hotfixPackageList, hotfixMethodList, exceptPackageList, exceptMethodList, isHotfixMethodLevel, isExceptMethodLevel, isForceInsertLambda);
    }

    @Override
    protected void insertCode(List<CtClass> box, File jarFile) throws CannotCompileException, IOException, NotFoundException {
        ZipOutputStream outStream = new JarOutputStream(new FileOutputStream(jarFile));
//        new ForkJoinPool().submit {
        for (CtClass ctClass : box) {
            if (isNeedInsertClass(ctClass.getName())) {
                //change class modifier
                ctClass.setModifiers(AccessFlag.setPublic(ctClass.getModifiers()));
                if (ctClass.isInterface() || ctClass.getDeclaredMethods().length < 1) {
                    //skip the unsatisfied class
                    zipFile(ctClass.toBytecode(), outStream, ctClass.getName().replaceAll("\\.", "/") + ".class");
                    continue;
                }

                boolean addIncrementalChange = false;
                for (CtBehavior ctBehavior : ctClass.getDeclaredBehaviors()) {
                    if (!addIncrementalChange) {
                        //insert the field
                        addIncrementalChange = true;
                        ClassPool classPool = ctBehavior.getDeclaringClass().getClassPool();
                        CtClass type = classPool.getOrNull(Constants.INTERFACE_NAME);
                        CtField ctField = new CtField(type, Constants.INSERT_FIELD_NAME, ctClass);
                        ctField.setModifiers(AccessFlag.PUBLIC | AccessFlag.STATIC);
                        ctClass.addField(ctField);
                    }
                    if (!isQualifiedMethod(ctBehavior)) {
                        continue;
                    }
                    //here comes the method will be inserted code
                    methodMap.put(ctBehavior.getLongName(), insertMethodCount.incrementAndGet());
                    try {
                        if (ctBehavior.getMethodInfo().isMethod()) {
                            CtMethod ctMethod = (CtMethod) ctBehavior;
                            boolean isStatic = (ctMethod.getModifiers() & AccessFlag.STATIC) != 0;
                            CtClass returnType = ctMethod.getReturnType();
                            String returnTypeString = returnType.getName();
                            //construct the code will be inserted in string format
                            String body = "Object argThis = null;";
                            if (!isStatic) {
                                body += "argThis = $0;";
                            }
                            String parametersClassType = getParametersClassType(ctMethod);
//                                body += "   if (com.meituan.robust.PatchProxy.isSupport(\$args, argThis, ${Constants.INSERT_FIELD_NAME}, $isStatic, " + methodMap.get(ctBehavior.longName) + ",${parametersClassType},${returnTypeString}.class)) {"
                            body += "   if (com.meituan.robust.PatchProxy.isSupport($args, argThis, " + Constants.INSERT_FIELD_NAME + ", " + isStatic +
                                    ", " + methodMap.get(ctBehavior.getLongName()) + "," + parametersClassType + "," + returnTypeString + ".class)) {";
                            body += getReturnStatement(returnTypeString, isStatic, methodMap.get(ctBehavior.getLongName()), parametersClassType, returnTypeString + ".class");
                            body += "   }";
                            //finish the insert-code body ,let`s insert it
                            ctBehavior.insertBefore(body);
                        }
                    } catch (Throwable t) {
                        //here we ignore the error
                        t.printStackTrace();
                        System.out.println("ctClass: " + ctClass.getName() + " error: " + t.getMessage());
                    }
                }
            }
            //zip the inserted-classes into output file
            zipFile(ctClass.toBytecode(), outStream, ctClass.getName().replaceAll("\\.", "/") + ".class");
        }
//        }.get()
        outStream.close();
    }

    private boolean isQualifiedMethod(CtBehavior it) throws CannotCompileException {
        if (it.getMethodInfo().isStaticInitializer()) {
            return false;
        }

        // synthetic 方法暂时不aop 比如AsyncTask 会生成一些同名 synthetic方法,对synthetic 以及private的方法也插入的代码，主要是针对lambda表达式
        if (!isForceInsertLambda && (it.getModifiers() & AccessFlag.SYNTHETIC) != 0 && !AccessFlag.isPrivate(it.getModifiers())) {
            return false;
        }
        if (it.getMethodInfo().isConstructor()) {
            return false;
        }

        if ((it.getModifiers() & AccessFlag.ABSTRACT) != 0) {
            return false;
        }
        if ((it.getModifiers() & AccessFlag.NATIVE) != 0) {
            return false;
        }
        if ((it.getModifiers() & AccessFlag.INTERFACE) != 0) {
            return false;
        }

        if (it.getMethodInfo().isMethod()) {
            if (AccessFlag.isPackage(it.getModifiers())) {
                it.setModifiers(AccessFlag.setPublic(it.getModifiers()));
            }
            boolean flag = isMethodWithExpression((CtMethod) it);
            if (!flag) {
                return false;
            }
        }
        //方法过滤
        if (isExceptMethodLevel && exceptMethodList != null) {
            for (String exceptMethod : exceptMethodList) {
                if (it.getName().matches(exceptMethod)) {
                    return false;
                }
            }
        }

        if (isHotfixMethodLevel && hotfixMethodList != null) {
            for (String name : hotfixMethodList) {
                if (it.getName().matches(name)) {
                    return true;
                }
            }
        }
        return !isHotfixMethodLevel;
    }

    private String getParametersClassType(CtMethod method) throws NotFoundException {
        if (method.getParameterTypes().length == 0) {
            return " null ";
        }
        StringBuilder parameterType = new StringBuilder();
        parameterType.append("new Class[]{");
        for (CtClass paramterClass : method.getParameterTypes()) {
            parameterType.append(paramterClass.getName()).append(".class,");
        }
        //remove last ','
        if (',' == parameterType.charAt(parameterType.length() - 1))
            parameterType.deleteCharAt(parameterType.length() - 1);
        parameterType.append("}");
        return parameterType.toString();
    }

    //判断代码中是否有方法调用
    private boolean isCallMethod = false;

    /**
     * 判断是否有方法调用
     *
     * @return 是否插桩
     */
    private boolean isMethodWithExpression(CtMethod ctMethod) throws CannotCompileException {
        isCallMethod = false;
        if (ctMethod == null) {
            return false;
        }

        ctMethod.instrument(new ExprEditor() {
            /**
             * Edits a <tt>new</tt> expression (overridable).
             * The default implementation performs nothing.
             *
             * @param e the <tt>new</tt> expression creating an object.
             */
//            public void edit(NewExpr e) throws CannotCompileException { isCallMethod = true; }

            /**
             * Edits an expression for array creation (overridable).
             * The default implementation performs nothing.
             *
             * @param a the <tt>new</tt> expression for creating an array.
             * @throws CannotCompileException
             */
            public void edit(NewArray a) throws CannotCompileException {
                isCallMethod = true;
            }

            /**
             * Edits a method call (overridable).
             *
             * The default implementation performs nothing.
             */
            public void edit(MethodCall m) throws CannotCompileException {
                isCallMethod = true;
            }

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
            public void edit(Instanceof i) throws CannotCompileException {
                isCallMethod = true;
            }

            /**
             * Edits an expression for explicit type casting (overridable).
             * The default implementation performs nothing.
             */
            public void edit(Cast c) throws CannotCompileException {
                isCallMethod = true;
            }

            /**
             * Edits a catch clause (overridable).
             * The default implementation performs nothing.
             */
            public void edit(Handler h) throws CannotCompileException {
                isCallMethod = true;
            }
        });
        return isCallMethod;
    }

    /**
     * 根据传入类型判断调用PathProxy的方法
     *
     * @param type         返回类型
     * @param isStatic     是否是静态方法
     * @param methodNumber 方法数
     * @return 返回return语句
     */
    private String getReturnStatement(String type, boolean isStatic, int methodNumber, String parametersClassType, String returnTypeString) {
        switch (type) {
            case Constants.CONSTRUCTOR:
                return "    com.meituan.robust.PatchProxy.accessDispatchVoid( $args, argThis, changeQuickRedirect, " + isStatic + ", " + methodNumber + "," + parametersClassType + "," + returnTypeString + ");  ";
            case Constants.LANG_VOID:
                return "    com.meituan.robust.PatchProxy.accessDispatchVoid( $args, argThis, changeQuickRedirect, " + isStatic + ", " + methodNumber + "," + parametersClassType + "," + returnTypeString + ");   return null;";

            case Constants.VOID:
                return "    com.meituan.robust.PatchProxy.accessDispatchVoid( $args, argThis, changeQuickRedirect, " + isStatic + ", " + methodNumber + "," + parametersClassType + "," + returnTypeString + ");   return ;";

            case Constants.LANG_BOOLEAN:
                return "   return ((java.lang.Boolean)com.meituan.robust.PatchProxy.accessDispatch( $args, argThis, changeQuickRedirect, " + isStatic + "," + methodNumber + "," + parametersClassType + "," + returnTypeString + "));";
            case Constants.BOOLEAN:
                return "   return ((java.lang.Boolean)com.meituan.robust.PatchProxy.accessDispatch($args, argThis, changeQuickRedirect, " + isStatic + "," + methodNumber + "," + parametersClassType + "," + returnTypeString + ")).booleanValue();";

            case Constants.INT:
                return "   return ((java.lang.Integer)com.meituan.robust.PatchProxy.accessDispatch( $args, argThis, changeQuickRedirect, " + isStatic + "," + methodNumber + "," + parametersClassType + "," + returnTypeString + ")).intValue();";
            case Constants.LANG_INT:
                return "   return ((java.lang.Integer)com.meituan.robust.PatchProxy.accessDispatch( $args, argThis, changeQuickRedirect, " + isStatic + "," + methodNumber + "," + parametersClassType + "," + returnTypeString + ")); ";

            case Constants.LONG:
                return "   return ((java.lang.Long)com.meituan.robust.PatchProxy.accessDispatch( $args, argThis, changeQuickRedirect, " + isStatic + "," + methodNumber + "," + parametersClassType + "," + returnTypeString + ")).longValue();";
            case Constants.LANG_LONG:
                return "   return ((java.lang.Long)com.meituan.robust.PatchProxy.accessDispatch( $args, argThis, changeQuickRedirect, " + isStatic + "," + methodNumber + "," + parametersClassType + "," + returnTypeString + "));";

            case Constants.DOUBLE:
                return "   return ((java.lang.Double)com.meituan.robust.PatchProxy.accessDispatch( $args, argThis, changeQuickRedirect, " + isStatic + "," + methodNumber + "," + parametersClassType + "," + returnTypeString + ")).doubleValue();";
            case Constants.LANG_DOUBLE:
                return "   return ((java.lang.Double)com.meituan.robust.PatchProxy.accessDispatch( $args, argThis, changeQuickRedirect, " + isStatic + "," + methodNumber + "," + parametersClassType + "," + returnTypeString + "));";

            case Constants.FLOAT:
                return "   return ((java.lang.Float)com.meituan.robust.PatchProxy.accessDispatch( $args, argThis, changeQuickRedirect, " + isStatic + "," + methodNumber + "," + parametersClassType + "," + returnTypeString + ")).floatValue();";
            case Constants.LANG_FLOAT:
                return "   return ((java.lang.Float)com.meituan.robust.PatchProxy.accessDispatch( $args, argThis, changeQuickRedirect, " + isStatic + "," + methodNumber + "," + parametersClassType + "," + returnTypeString + "));";

            case Constants.SHORT:
                return "   return ((java.lang.Short)com.meituan.robust.PatchProxy.accessDispatch( $args, argThis, changeQuickRedirect, " + isStatic + "," + methodNumber + "," + parametersClassType + "," + returnTypeString + ")).shortValue();";
            case Constants.LANG_SHORT:
                return "   return ((java.lang.Short)com.meituan.robust.PatchProxy.accessDispatch( $args, argThis, changeQuickRedirect, " + isStatic + "," + methodNumber + "," + parametersClassType + "," + returnTypeString + "));";

            case Constants.BYTE:
                return "   return ((java.lang.Byte)com.meituan.robust.PatchProxy.accessDispatch( $args, argThis, changeQuickRedirect, " + isStatic + "," + methodNumber + "," + parametersClassType + "," + returnTypeString + ")).byteValue();";
            case Constants.LANG_BYTE:
                return "   return ((java.lang.Byte)com.meituan.robust.PatchProxy.accessDispatch( $args, argThis, changeQuickRedirect, " + isStatic + "," + methodNumber + "," + parametersClassType + "," + returnTypeString + "));";
            case Constants.CHAR:
                return "   return ((java.lang.Character)com.meituan.robust.PatchProxy.accessDispatch( $args, argThis, changeQuickRedirect, " + isStatic + "," + methodNumber + "," + parametersClassType + "," + returnTypeString + ")).charValue();";
            case Constants.LANG_CHARACTER:
                return "   return ((java.lang.Character)com.meituan.robust.PatchProxy.accessDispatch( $args, argThis, changeQuickRedirect, " + isStatic + "," + methodNumber + "," + parametersClassType + "," + returnTypeString + "));";
            default:
                return "   return (" + type + ")com.meituan.robust.PatchProxy.accessDispatch( $args, argThis, changeQuickRedirect, " + isStatic + "," + methodNumber + "," + parametersClassType + "," + returnTypeString + ");";
        }
    }
}
