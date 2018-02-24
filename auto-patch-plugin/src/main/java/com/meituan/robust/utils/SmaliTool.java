package com.meituan.robust.utils;

import com.meituan.robust.Constants;
import com.meituan.robust.autopatch.ClassMapping;
import com.meituan.robust.autopatch.Config;
import com.meituan.robust.autopatch.NameManger;
import com.meituan.robust.autopatch.ReadMapping;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;

import javassist.CtClass;
import javassist.CtMethod;
import javassist.CtPrimitiveType;

import static com.meituan.robust.Constants.PACKNAME_END;
import static com.meituan.robust.Constants.PACKNAME_START;
import static com.meituan.robust.autopatch.Config.classPool;
import static com.meituan.robust.autopatch.Config.invokeSuperMethodMap;

/**
 * Created by mivanzhang on 17/2/8.
 */

public class SmaliTool {
    private static SmaliTool instance;

    public static SmaliTool getInstance() {
        if (instance == null) {
            instance = new SmaliTool();
        }
        return instance;
    }

    private SmaliTool() {

    }

    public void dealObscureInSmali() {
        File diretory = new File(Config.robustGenerateDirectory + "classout" + File.separator + Config.patchPackageName.replaceAll("\\.", Matcher.quoteReplacement(File.separator)));
        if (!diretory.isDirectory() || diretory == null) {
            throw new RuntimeException(Config.robustGenerateDirectory + Config.patchPackageName.replaceAll(".", Matcher.quoteReplacement(File.separator)) + " contains no smali file error!! ");
        }
        List<File> smaliFileList = covertPathToFile(Config.robustGenerateDirectory + "classout" + File.separator, Config.newlyAddedClassNameList);
        for (File file : diretory.listFiles()) {
            smaliFileList.add(file);
        }
        for (File file : smaliFileList) {
            BufferedWriter writer = null;
            BufferedReader reader = null;
            StringBuilder fileContent = new StringBuilder();
            try {
                reader = new BufferedReader(new FileReader(file));
                String line;
                int lineNo = 1;
                // 一次读入一行，直到读入null为文件结束
                while ((line = reader.readLine()) != null) {
                    // 显示行号
                    fileContent.append(dealWithSmaliLine(line, JavaUtils.getFullClassNameFromFile(file.getPath())) + "\n");
                    lineNo++;
                }
                writer = new BufferedWriter(new FileWriter(file));
                writer.write(fileContent.toString());
                writer.flush();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
                if (writer != null) {
                    try {
                        writer.close();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        }
    }

    private List<File> covertPathToFile(String directory, List<String> packNameList) {
        if (packNameList == null) {
            return new ArrayList<>();
        }
        List<File> fileList = new ArrayList<>();
        for (String packname : packNameList) {
            fileList.add(new File(directory + packname.replaceAll("\\.", Matcher.quoteReplacement(File.separator)) + ".smali"));
        }
        return fileList;
    }

    private String dealWithSmaliLine(final String line, String fullClassName) {

        if (null == line || line.length() < 1 || line.startsWith("#")) {
            return line;
        }

        // 主要针对 @Add 的新增类，需要为其创建混淆规则，为以后混淆方法名做准备。
        if (Config.newlyAddedClassNameList.contains(fullClassName)) {
            if (ReadMapping.getInstance().getClassMapping(fullClassName) == null) {
                ClassMapping classMapping = new ClassMapping();
                classMapping.setClassName(fullClassName);
                classMapping.setValueName(fullClassName);
                ReadMapping.getInstance().setClassMapping(fullClassName, classMapping);
            }

            if (line.startsWith(".super") || line.startsWith(".implements")) {
                List<String> packNameFromSmaliLine = getPackNameFromSmaliLine(line);
                if (packNameFromSmaliLine.size() > 0) {
                    String className = packNameFromSmaliLine.get(0).replaceAll("/", "\\.");
                    ClassMapping superClassMapping = ReadMapping.getInstance().getClassMapping(className);
                    ClassMapping newClassMapping = ReadMapping.getInstance().getClassMapping(fullClassName);
                    if (superClassMapping != null) {
                        for (String key : superClassMapping.getMemberMapping().keySet()) {
                            // 理论上.super的出现的比.implement出现早，而且继承的混淆规则应该更准确，所以在这里谁先进map谁优先。
                            if (!newClassMapping.getMemberMapping().containsKey(key)) {
                                newClassMapping.getMemberMapping().put(key, superClassMapping.getMemberMapping().get(key));
                            }
                        }
                    }
                }
            }
        }

        String result = invokeSuperMethodInSmali(line, fullClassName);

        int packageNameIndex;
        int previousPackageNameIndex = 0;
        List<String> packageNameList = getPackNameFromSmaliLine(result);
        Collections.sort(packageNameList, new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return o2.length() - o1.length();
            }
        });

        for (int index = 0; packageNameList != null && index < packageNameList.size(); index++) {

            if (result.indexOf(packageNameList.get(index)) != result.lastIndexOf(packageNameList.get(index))) {
                packageNameIndex = result.indexOf(packageNameList.get(index), previousPackageNameIndex);
                previousPackageNameIndex = packageNameIndex + packageNameList.get(index).length();
            } else {
                packageNameIndex = result.indexOf(packageNameList.get(index));
            }

            //invoke-virtual {v0, v5, v6, p0}, Landroid/support/v4/app/LoaderManager;->initLoader(ILandroid/os/Bundle;Landroid/support/v4/app/bi;)Landroid/support/v4/content/Loader;
            if (result.contains("invoke") &&
                    (packageNameIndex + packageNameList.get(index).length() + 3 < result.length()) &&
                    result.substring(packageNameIndex + packageNameList.get(index).length() + 1, packageNameIndex + packageNameList.get(index).length() + 3).equals("->")) {
                //方法调用的替换
                result = result.replace(
                        result.substring(packageNameIndex + packageNameList.get(index).length() + 3, result.indexOf(")") + 1),
                        getObscuredMethodSignure(result.substring(packageNameIndex + packageNameList.get(index).length() + 3),
                                packageNameList.get(index).replaceAll("/", "\\."))
                );
            } else if (result.contains("->") && (result.indexOf("(") == -1) && ((packageNameIndex + packageNameList.get(index).length() + 3) < result.length())) {
                // 字段处理
                //sget-object v4, Lcom/sankuai/meituan/fingerprint/FingerprintConfig;->accelerometerInfoList:Ljava/util/List;
                String fieldName = result.substring(packageNameIndex + packageNameList.get(index).length() + 3, result.lastIndexOf(":"));
                // 前后都加上 "->" 是为了避免类名中包含字段名时，类名被误修改导致patch生成错误
                result = result.replaceFirst("->" + fieldName, "->" + getObscuredMemberName(packageNameList.get(index).replaceAll("/", "\\."), fieldName));
            }
        }

        // 处理@Add新增类的方法名混淆
        if (Config.newlyAddedClassNameList.contains(fullClassName)) {
            if (result.startsWith(".method ") &&
                    !result.contains("constructor <init>") &&
                    !result.contains("constructor <clinit>")) {
                System.out.println("new Add class: line = " + line);
                //.method public onFailure(Lcom/bytedance/retrofit2/Call;Ljava/lang/Throwable;)V
                int start = result.indexOf("(");
                for (; start >= 0; start--) {
                    if (line.charAt(start) == ' ') {
                        break;
                    }
                }
                String methodSignature = result.substring(start + 1);
                // 注意getObscuredMethodSignure并没有混淆返回值，不能在下面这句话之后直接返回。还需要最后混淆一次
                result = result.replace(methodSignature.substring(0, methodSignature.indexOf(")") + 1),
                        getObscuredMethodSignure(methodSignature, fullClassName));
            }
        }

        for (int index = 0; packageNameList != null && index < packageNameList.size(); index++) {
            result = result.replace(packageNameList.get(index), getObscuredClassName(packageNameList.get(index)));
        }
        return result;
    }

    private boolean isInStaticRobustMethod = false;

    private String invokeSuperMethodInSmali(final String line, String fullClassName) {

        if (line.startsWith(".method public static staticRobust")) {
            isInStaticRobustMethod = true;
        }
        String result = line;
        String returnType;
        List<CtMethod> invokeSuperMethodList = invokeSuperMethodMap.get(NameManger.getInstance().getPatchNameMap().get(fullClassName));
        if (isInStaticRobustMethod && line.contains(Constants.SMALI_INVOKE_VIRTUAL_COMMAND)) {
            for (CtMethod ctMethod : invokeSuperMethodList) {
                //java method signure
                if ((ctMethod.getName().replaceAll("\\.", "/") + ctMethod.getSignature().subSequence(0, ctMethod.getSignature().indexOf(")") + 1)).equals(getMethodSignureInSmaliLine(line))) {
                    result = line.replace(Constants.SMALI_INVOKE_VIRTUAL_COMMAND, Constants.SMALI_INVOKE_SUPER_COMMAND);
                    try {
                        if (!ctMethod.getReturnType().isPrimitive()) {
                            returnType = "L" + ctMethod.getReturnType().getName().replaceAll("\\.", "/");
                        } else {
                            returnType = String.valueOf(((CtPrimitiveType) ctMethod.getReturnType()).getDescriptor());
                        }
                        if (NameManger.getInstance().getPatchNameMap().get(fullClassName).equals(fullClassName)) {
                            result = result.replace("p0", "p1");
                        }
                        String fullClassNameInSmali = ctMethod.getDeclaringClass().getClassPool().get(fullClassName).getSuperclass().getName().replaceAll("\\.", "/");
                        result = result.replace(result.substring(result.indexOf(PACKNAME_START) + 1, result.indexOf(PACKNAME_END)), fullClassNameInSmali);
                        result = result.substring(0, result.indexOf(")") + 1) + returnType;
                        if (!ctMethod.getReturnType().isPrimitive()) {
                            result += ";";
                        }
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
        if (isInStaticRobustMethod && line.startsWith(".end method")) {
            isInStaticRobustMethod = false;
        }
//        System.out.println("  result is    " + result);
        return result;
    }

    private String getMethodSignureInSmaliLine(String s) {
        return s.substring(s.indexOf("->") + 2, s.indexOf(")") + 1);
    }

    private List<String> getPackNameFromSmaliLine(String line) {
        ArrayList<String> packageNameList = new ArrayList<>();
        if (null == line) {
            return packageNameList;
        }
        int startIndex;
        int endIndex;
        for (; line != null && line.length() > 0; ) {
            startIndex = 0;
            for (; ; ) {
                startIndex = line.indexOf(Constants.PACKNAME_START, startIndex + 1);
                if (startIndex < 0 || !Character.isLetter(line.charAt(startIndex - 1)) || line.lastIndexOf(Constants.PACKNAME_START) == startIndex) {
                    break;
                }
            }
            endIndex = line.indexOf(Constants.PACKNAME_END, startIndex);
            if (startIndex < 0 || endIndex < 0) {
                break;
            }
            packageNameList.add(line.substring(startIndex + 1, endIndex));
            line = line.substring(endIndex);
        }

//        if (packageNameList.size() > 0)
//            System.out.println("getPackNameFromSmaliLine  " + packageNameList);
        return packageNameList;

    }

    public static void main(String[] args) {
        SmaliTool smaliUitils = new SmaliTool();
        smaliUitils.getObscuredMethodSignure("invokeReflectConstruct(Ljava/lang/String;[Ljava/lang/Object;[Ljava/lang/Class;)Ljava/lang/Object;", "com.meituan.second");
    }

    private String getObscuredMethodSignure(final String line, String className) {

        if (className.endsWith(Constants.PATCH_SUFFIX) && Config.modifiedClassNameList.contains(className.substring(0, className.indexOf(Constants.PATCH_SUFFIX)))) {
            className = className.substring(0, className.indexOf(Constants.PATCH_SUFFIX));
        }
        StringBuilder methodSignureBuilder = new StringBuilder();
        methodSignureBuilder.append(line.substring(0, line.indexOf("(") + 1));
        String parameter = line.substring(line.indexOf("("), line.indexOf(")") + 1);
        int endIndex = line.indexOf(")");
        String methodSigure = line.substring(0, endIndex + 1);
        //invokeReflectConstruct(Ljava/lang/String;[Ljava/lang/Object;[Ljava/lang/Class;)Ljava/lang/Object;
        boolean isArray = false;
        for (int index = line.indexOf("(") + 1; index < endIndex; index++) {
            if (Constants.PACKNAME_START.equals(String.valueOf(methodSigure.charAt(index))) && methodSigure.contains(Constants.PACKNAME_END)) {
                methodSignureBuilder.append(methodSigure.substring(index + 1, methodSigure.indexOf(Constants.PACKNAME_END, index)).replaceAll("/", "\\."));
                if (isArray) {
                    methodSignureBuilder.append("[]");
                    isArray = false;
                }
                index = methodSigure.indexOf(";", index);
                methodSignureBuilder.append(",");
            }
            if (Constants.PRIMITIVE_TYPE.contains(String.valueOf(methodSigure.charAt(index)))) {

                switch (methodSigure.charAt(index)) {
                    case 'Z':
                        methodSignureBuilder.append("boolean");
                        break;
                    case 'C':
                        methodSignureBuilder.append("char");
                        break;
                    case 'B':
                        methodSignureBuilder.append("byte");
                        break;
                    case 'S':
                        methodSignureBuilder.append("short");
                        break;
                    case 'I':
                        methodSignureBuilder.append("int");
                        break;
                    case 'J':
                        methodSignureBuilder.append("long");
                        break;
                    case 'F':
                        methodSignureBuilder.append("float");
                        break;
                    case 'D':
                        methodSignureBuilder.append("double");
                        break;
                    case 'V':
                        methodSignureBuilder.append("void");
                        break;
                    default:
                        break;
                }
                if (isArray) {
                    methodSignureBuilder.append("[]");
                    isArray = false;
                }
                methodSignureBuilder.append(",");
            }

            if (Constants.ARRAY_TYPE.equals(String.valueOf(methodSigure.charAt(index)))) {
                isArray = true;
            }

        }

        List<String> returnTypeList = gePackageNameFromSmaliLine(line.substring(endIndex + 1));
        if (String.valueOf(methodSignureBuilder.charAt(methodSignureBuilder.toString().length() - 1)).equals(","))
            methodSignureBuilder.deleteCharAt(methodSignureBuilder.toString().length() - 1);
        methodSignureBuilder.append(")");
        String obscuredMethodSignure = methodSignureBuilder.toString();
        String obscuredMethodName = getObscuredMemberName(className, ReadMapping.getInstance().getMethodSignureWithReturnType(returnTypeList.get(0), obscuredMethodSignure));
        obscuredMethodSignure = obscuredMethodName + parameter;
//        System.out.println("getObscuredMethodSignure is "+obscuredMethodSignure.substring(0, obscuredMethodSignure.indexOf("(")) + parameter);
        return obscuredMethodSignure.substring(0, obscuredMethodSignure.indexOf("(")) + parameter;
    }

    private List<String> gePackageNameFromSmaliLine(String smaliLine) {
        List<String> packageNameList = new ArrayList<>();
        for (int index = 0; index < smaliLine.length(); index++) {
            if (Constants.PACKNAME_START.equals(String.valueOf(smaliLine.charAt(index))) && smaliLine.indexOf(Constants.PACKNAME_END) != -1) {
                packageNameList.add(smaliLine.substring(index + 1, smaliLine.indexOf(Constants.PACKNAME_END, index)).replaceAll("/", "\\."));
                index = smaliLine.indexOf(";", index);
            }
            if (Constants.PRIMITIVE_TYPE.contains(String.valueOf(smaliLine.charAt(index)))) {

                switch (smaliLine.charAt(index)) {
                    case 'Z':
                        packageNameList.add("boolean");
                        break;
                    case 'C':
                        packageNameList.add("char");
                        break;
                    case 'B':
                        packageNameList.add("byte");
                        break;
                    case 'S':
                        packageNameList.add("short");
                        break;
                    case 'I':
                        packageNameList.add("int");
                        break;
                    case 'J':
                        packageNameList.add("long");
                        break;
                    case 'F':
                        packageNameList.add("float");
                        break;
                    case 'D':
                        packageNameList.add("double");
                        break;
                    case 'V':
                        packageNameList.add("void");
                        break;
                    default:
                        break;
                }
            }

        }
        return packageNameList;
    }

    private String getObscuredMemberName(String className, String memberName) {

        ClassMapping classMapping = ReadMapping.getInstance().getClassMapping(className);
        if (classMapping == null) {
            System.out.println("Warning: getObscuredMemberName  class  name " + className + "   member name is  " + memberName + "  robust can not find in mapping!!! ");
            return JavaUtils.eradicatReturnType(memberName);
        }

        while (classMapping != null && !"java.lang.Object".equals(classMapping.getClassName())) {
            if (classMapping.getMemberMapping().get(memberName) != null) {
                return classMapping.getMemberMapping().get(memberName);
            } else {
                try {
                    CtClass superClass = classPool.get(classMapping.getClassName()).getSuperclass();
                    while (ReadMapping.getInstance().getClassMapping(superClass.getName()) == null && !"java.lang.Object".equals(superClass.getName())) {
                        superClass = superClass.getSuperclass();
                    }
                    classMapping = ReadMapping.getInstance().getClassMapping(superClass.getName());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return JavaUtils.eradicatReturnType(memberName);
    }

    private String getObscuredClassName(String className) {
        ClassMapping classMapping = ReadMapping.getInstance().getClassMapping(className.replaceAll("/", "\\."));
        if (null == classMapping || classMapping.getValueName() == null) {
            return className;
        }
        return classMapping.getValueName().replaceAll("\\.", "/");


    }
}
