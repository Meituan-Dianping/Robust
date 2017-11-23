package com.meituan.robust.autopatch;

import com.meituan.robust.Constants;

import javassist.CtClass;
import javassist.CtMethod;
import javassist.bytecode.ClassFile;

import static com.meituan.robust.autopatch.Config.classPool;
import static javassist.CtNewMethod.make;

/**
 * Created by mivanzhang on 17/2/9.
 * <p>
 * create patch info classes which describes patch dex
 */

public class PatchesInfoFactory {
    private static PatchesInfoFactory patchesInfoFactory = new PatchesInfoFactory();

    private PatchesInfoFactory() {

    }

    private CtClass createPatchesInfoClass() {
        try {
            CtClass ctPatchesInfoImpl = classPool.makeClass(Config.patchPackageName + ".PatchesInfoImpl");
            ctPatchesInfoImpl.getClassFile().setMajorVersion(ClassFile.JAVA_7);
            ctPatchesInfoImpl.setInterfaces(new CtClass[]{classPool.get("com.meituan.robust.PatchesInfo")});
            StringBuilder methodBody = new StringBuilder();
            methodBody.append("public java.util.List getPatchedClassesInfo() {");
            methodBody.append("  java.util.List patchedClassesInfos = new java.util.ArrayList();");
            for (int i = 0; i < Config.modifiedClassNameList.size(); i++) {
                if (Constants.OBSCURE) {
                    methodBody.append("com.meituan.robust.PatchedClassInfo patchedClass" + i + " = new com.meituan.robust.PatchedClassInfo(\"" + ReadMapping.getInstance().getClassMappingOrDefault(Config.modifiedClassNameList.get(i)).getValueName() + "\",\"" + NameManger.getInstance().getPatchControlName(Config.modifiedClassNameList.get(i).substring(Config.modifiedClassNameList.get(i).lastIndexOf('.') + 1)) + "\");");
                } else {
                    methodBody.append("com.meituan.robust.PatchedClassInfo patchedClass" + i + " = new com.meituan.robust.PatchedClassInfo(\"" + Config.modifiedClassNameList.get(i) + "\",\"" + NameManger.getInstance().getPatchControlName(Config.modifiedClassNameList.get(i).substring(Config.modifiedClassNameList.get(i).lastIndexOf('.') + 1)) + "\");");
                }
                methodBody.append("patchedClassesInfos.add(patchedClass" + i + ");");
            }
            methodBody.append(Constants.ROBUST_UTILS_FULL_NAME + ".isThrowable=!" + Config.catchReflectException + ";");
            methodBody.append("return patchedClassesInfos;\n" +
                    "    }");
            CtMethod m = make(methodBody.toString(), ctPatchesInfoImpl);
            ctPatchesInfoImpl.addMethod(m);
            return ctPatchesInfoImpl;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public static CtClass createPatchesInfo() {
        return patchesInfoFactory.createPatchesInfoClass();
    }
}
