package com.meituan.robust.autopatch;

import com.meituan.robust.Constants;
import com.meituan.robust.utils.JavaUtils;

import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;
import javassist.bytecode.AccessFlag;

import static com.meituan.robust.autopatch.Config.classPool;

/**
 * Created by mivanzhang on 17/2/9.
 *
 * create patch control classes,which dispatch patch methods
 */

public class PatchesControlFactory {
    private static PatchesControlFactory patchesControlFactory = new PatchesControlFactory();

    private PatchesControlFactory() {

    }

    private CtClass createControlClass(CtClass modifiedClass) throws Exception {
        CtClass patchClass = classPool.get(NameManger.getInstance().getPatchName(modifiedClass.getName()));
        patchClass.defrost();
        CtClass controlClass = classPool.getAndRename(Constants.PATCH_TEMPLATE_FULL_NAME, NameManger.getInstance().getPatchControlName(modifiedClass.getSimpleName()));
        StringBuilder getRealParameterMethodBody = new StringBuilder();
        getRealParameterMethodBody.append("public Object getRealParameter(Object parameter) {");
        getRealParameterMethodBody.append("if(parameter instanceof " + modifiedClass.getName() + "){");
        getRealParameterMethodBody.
                append("return new " + patchClass.getName() + "(parameter);");
        getRealParameterMethodBody.append("}");
        getRealParameterMethodBody.append("return parameter;}");
        controlClass.addMethod(CtMethod.make(getRealParameterMethodBody.toString(), controlClass));
        controlClass.getDeclaredMethod("accessDispatch").insertBefore(getAccessDispatchMethodBody(patchClass, modifiedClass.getName()));
        controlClass.getDeclaredMethod("isSupport").insertBefore(getIsSupportMethodBody(patchClass, modifiedClass.getName()));
        controlClass.defrost();
        return controlClass;
    }

    private
    static String getAccessDispatchMethodBody(CtClass patchClass, String modifiedClassName) throws NotFoundException {
        StringBuilder accessDispatchMethodBody = new StringBuilder();
        if(Config.catchReflectException){
            accessDispatchMethodBody.append("try{");
        }
        if (Constants.isLogging) {
            accessDispatchMethodBody.append("  android.util.Log.d(\"robust\",\"arrivied in AccessDispatch \"+methodName+\" paramArrayOfObject  \" +paramArrayOfObject);");
        }
        //create patch instance
        accessDispatchMethodBody.append(patchClass.getName() + " patch= null;\n");
        accessDispatchMethodBody.append(" String isStatic=$1.split(\":\")[2];");
        accessDispatchMethodBody.append(" if (isStatic.equals(\"false\")) {\n");
        accessDispatchMethodBody.append(" if (keyToValueRelation.get(paramArrayOfObject[paramArrayOfObject.length - 1]) == null) {\n");
        if (Constants.isLogging) {
            accessDispatchMethodBody.append("  android.util.Log.d(\"robust\",\"keyToValueRelation not contain\" );");
        }
        accessDispatchMethodBody.append("patch=new " + patchClass.getName() + "(paramArrayOfObject[paramArrayOfObject.length - 1]);\n");
        accessDispatchMethodBody.append(" keyToValueRelation.put(paramArrayOfObject[paramArrayOfObject.length - 1], null);\n");
        accessDispatchMethodBody.append("}else{");
        accessDispatchMethodBody.append("patch=(" + patchClass.getName() + ") keyToValueRelation.get(paramArrayOfObject[paramArrayOfObject.length - 1]);\n");
        accessDispatchMethodBody.append("}");
        accessDispatchMethodBody.append("}\n");
        accessDispatchMethodBody.append("else{");
        if (Constants.isLogging) {
            accessDispatchMethodBody.append("  android.util.Log.d(\"robust\",\"static method forward \" );");
        }
        accessDispatchMethodBody.append("patch=new " + patchClass.getName() + "(null);\n");
        accessDispatchMethodBody.append("}");
        accessDispatchMethodBody.append("String methodNo=$1.split(\":\")[3];\n");
        if (Constants.isLogging) {
            accessDispatchMethodBody.append("  android.util.Log.d(\"robust\",\"assemble method number  is  \" + methodNo);");
        }

//        patchClass.declaredMethods.each {
        for (CtMethod method : patchClass.getDeclaredMethods()) {
            CtClass[] parametertypes = method.getParameterTypes();
            String methodSignure = JavaUtils.getJavaMethodSignure(method).replaceAll(patchClass.getName(), modifiedClassName);
            String methodLongName = modifiedClassName + "." + methodSignure;
            Integer methodNumber = Config.methodMap.get(methodLongName);
            //just Forward methods with methodNumber
            if (methodNumber != null) {
                accessDispatchMethodBody.append(" if((\"" + methodNumber + "\").equals(methodNo)){\n");
                if (Constants.isLogging) {
                    accessDispatchMethodBody.append("  android.util.Log.d(\"robust\",\"invoke method is " + method.getLongName() + " \" );");
                }
                String methodName = method.getName();
                if (AccessFlag.isPrivate(method.getModifiers())) {
                    methodName = Constants.ROBUST_PUBLIC_SUFFIX + method.getName();
                }
                if (method.getReturnType().getName().equals("void")) {
                    accessDispatchMethodBody.append("(patch." + methodName + "(");
                } else {
                    switch (method.getReturnType().getName()) {
                        case "boolean":
                            accessDispatchMethodBody.append("return Boolean.valueOf(patch." + methodName + "(");
                            break;
                        case "byte":
                            accessDispatchMethodBody.append("return Byte.valueOf(patch." + methodName + "(");
                            break;
                        case "char":
                            accessDispatchMethodBody.append("return Character.valueOf(patch." + methodName + "(");
                            break;
                        case "double":
                            accessDispatchMethodBody.append("return Double.valueOf(patch." + methodName + "(");
                            break;
                        case "float":
                            accessDispatchMethodBody.append("return Float.valueOf(patch." + methodName + "(");
                            break;
                        case "int":
                            accessDispatchMethodBody.append("return Integer.valueOf(patch." + methodName + "(");
                            break;
                        case "long":
                            accessDispatchMethodBody.append("return Long.valueOf(patch." + methodName + "(");
                            break;
                        case "short":
                            accessDispatchMethodBody.append("return Short.valueOf(patch." + methodName + "(");
                            break;
                        default:
                            accessDispatchMethodBody.append("return (patch." + methodName + "(");
                            break;
                    }
                }
                for (int index = 0; index < parametertypes.length; index++) {
                    if (booleanPrimeType(parametertypes[index].getName())){
                        accessDispatchMethodBody.append("((" + JavaUtils.getWrapperClass(parametertypes[index].getName()) + ") (fixObj(paramArrayOfObject[" + index + "]))");
                        accessDispatchMethodBody.append(")" + JavaUtils.wrapperToPrime(parametertypes[index].getName()));
                        if (index != parametertypes.length - 1) {
                            accessDispatchMethodBody.append(",");
                        }
                    } else {
                    accessDispatchMethodBody.append("((" + JavaUtils.getWrapperClass(parametertypes[index].getName()) + ") (paramArrayOfObject[" + index + "])");
                    accessDispatchMethodBody.append(")" + JavaUtils.wrapperToPrime(parametertypes[index].getName()));
                    if (index != parametertypes.length - 1) {
                        accessDispatchMethodBody.append(",");
                    }
                    }
                }
                accessDispatchMethodBody.append("));}\n");
            }
        }
        if(Config.catchReflectException){
            accessDispatchMethodBody.append(" } catch (Throwable e) {");
            accessDispatchMethodBody.append(" e.printStackTrace();}");
        }
        return accessDispatchMethodBody.toString();
    }

    private static String getIsSupportMethodBody(CtClass patchClass, String modifiedClassName) throws NotFoundException {
        StringBuilder isSupportBuilder = new StringBuilder();
        StringBuilder methodsIdBuilder = new StringBuilder();
        if (Constants.isLogging) {
            isSupportBuilder.append("  android.util.Log.d(\"robust\",\"arrivied in isSupport \"+methodName+\" paramArrayOfObject  \" +paramArrayOfObject);");
        }
        isSupportBuilder.append("String methodNo=$1.split(\":\")[3];\n");
        if (Constants.isLogging) {
            isSupportBuilder.append("  android.util.Log.d(\"robust\",\"in isSupport assemble method number  is  \" + methodNo);");
        }
        for (CtMethod method : patchClass.getDeclaredMethods()) {
            String methodSignure = JavaUtils.getJavaMethodSignure(method).replaceAll(patchClass.getName(), modifiedClassName);
            String methodLongName = modifiedClassName + "." + methodSignure;
            Integer methodNumber = Config.methodMap.get(methodLongName);
            //just Forward methods with methodNumber
            if (methodNumber != null) {
                // 一前一后的冒号作为匹配锚点，只有一边有的话可能会有多重匹配的bug
                methodsIdBuilder.append(":" + methodNumber + ":");
            }
        }

        if (Constants.isLogging) {
            isSupportBuilder.append("  android.util.Log.d(\"robust\",\"arrivied in isSupport \"+methodName+\" paramArrayOfObject  \" +paramArrayOfObject+\" isSupport result is \"+\"" + methodsIdBuilder.toString() + "\".contains(\":\" + methodNo + \":\"));");
        }
        isSupportBuilder.append("return \"" + methodsIdBuilder.toString() + "\".contains(\":\" + methodNo + \":\");");
        return isSupportBuilder.toString();
    }


    public static CtClass createPatchesControl(CtClass modifiedClass) throws Exception {
        return patchesControlFactory.createControlClass(modifiedClass);
    }

    public static boolean booleanPrimeType(String typeName) {
        return "boolean".equals(typeName);
    }

}
