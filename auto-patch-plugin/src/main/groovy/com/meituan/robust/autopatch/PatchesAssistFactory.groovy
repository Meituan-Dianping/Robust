package com.meituan.robust.autopatch

import com.meituan.robust.utils.JavaUtils
import javassist.CtClass
import javassist.CtMethod
import javassist.bytecode.ClassFile

class PatchesAssistFactory {
    def
    static createAssistClass(CtClass modifiedClass, String patchClassName, CtMethod removeMethod) {
        CtClass assistClass = Config.classPool.getOrNull(NameManger.getInstance().getAssistClassName(patchClassName))
        if (assistClass == null) {
            assistClass = Config.classPool.makeClass(NameManger.getInstance().getAssistClassName(patchClassName))
            assistClass.getClassFile().setMajorVersion(ClassFile.JAVA_7);
            if (modifiedClass.getSuperclass() != null)
                assistClass.setSuperclass(modifiedClass.getSuperclass())
        }
        if(assistClass.isFrozen()){
            assistClass.defrost();
        }
        StringBuilder staticMethodBuidler = new StringBuilder();
        if (removeMethod.parameterTypes.length > 0) {
            staticMethodBuidler.append("public static  " + removeMethod.returnType.name + "   " + ReflectUtils.getStaticSuperMethodName(removeMethod.getName())
                    + "(" + patchClassName + " patchInstance," + modifiedClass.getName() + " modifiedInstance," + JavaUtils.getParameterSignure(removeMethod) + "){");

        } else {
            staticMethodBuidler.append("public static  " + removeMethod.returnType.name + "   " + ReflectUtils.getStaticSuperMethodName(removeMethod.getName())
                    + "(" + patchClassName + " patchInstance," + modifiedClass.getName() + " modifiedInstance){");

        }
        staticMethodBuidler.append(" return patchInstance." + removeMethod.getName() + "(" + JavaUtils.getParameterValue(removeMethod.getParameterTypes().length) + ");");
        staticMethodBuidler.append("}");

        CtMethod ctMethod = CtMethod.make(staticMethodBuidler.toString(), assistClass);
        Config.addedSuperMethodList.add(ctMethod);
        assistClass.addMethod(ctMethod);
        List<CtMethod> superList = Config.invokeSuperMethodMap.getOrDefault(NameManger.getInstance().getAssistClassName(patchClassName), new ArrayList());
        superList.add(removeMethod)
        Config.invokeSuperMethodMap.put(NameManger.getInstance().getAssistClassName(patchClassName), superList);
        return assistClass;
    }
}
