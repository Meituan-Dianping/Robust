package com.meituan.robust.utils;

import com.meituan.robust.Constants;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtField;
import javassist.CtMethod;
import javassist.CtNewConstructor;
import javassist.NotFoundException;

import static com.meituan.robust.Constants.ORIGINCLASS;

/**
 * Created by mivanzhang on 16/11/25.
 */

public class JavaUtils {

//    public static void removeJarFromLibs() {
//        File file;
//        for (String libName : LIB_NAME_ARRAY) {
//            file = new File(AutoPatchTransform.ROBUST_DIR + libName);
//            if (file.exists()) {
//                file.delete();
//            }
//        }
//    }

    public static Object getMapFromZippedFile(String path) {
        File file = new File(path);
        Object result = null;
        try {
            if (file.exists()) {
                FileInputStream fileIn = new FileInputStream(file);
                GZIPInputStream gzipIn = new GZIPInputStream(fileIn);
                ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
                int count;
                byte[] data = new byte[1024];
                while ((count = gzipIn.read(data, 0, 1024)) != -1) {
                    byteOut.write(data, 0, count);
                }
                ByteArrayInputStream byteIn = new ByteArrayInputStream(byteOut.toByteArray());
                ObjectInputStream oi = new ObjectInputStream(byteIn);
                result = oi.readObject();
                fileIn.close();
                gzipIn.close();
                oi.close();
            } else {
                throw new RuntimeException("getMapFromZippedFile error,file doesn't exist ,path is " + path);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("getMapFromZippedFile from " + path + "  error ");
        }
        return result;
    }

    public static int copy(InputStream input, OutputStream output) throws IOException {
        long count = copyLarge(input, output);
        return count > 2147483647L ? -1 : (int) count;
    }

    private static long copyLarge(InputStream input, OutputStream output) throws IOException {
        byte[] buffer = new byte[4096];
        long count = 0L;

        int n1;
        for (boolean n = false; -1 != (n1 = input.read(buffer)); count += (long) n1) {
            output.write(buffer, 0, n1);
        }

        return count;
    }

    public static String fileMd5(File file) {
        if (!file.isFile()) {
            return "";
        }
        MessageDigest digest = null;
        byte[] buffer = new byte[4096];
        int len;
        try {
            digest = MessageDigest.getInstance("MD5");
            FileInputStream inputStream = new FileInputStream(file);
            while ((len = inputStream.read(buffer, 0, 1024)) != -1) {
                digest.update(buffer, 0, len);
            }
            inputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }

        BigInteger bigInt = new BigInteger(1, digest.digest());
        return bigInt.toString(16);
    }

    public static String getWrapperClass(String typeName) {
        String warpperType = typeName;
        switch (typeName) {
            case "boolean":
                warpperType = "java.lang.Boolean";
                break;
            case "byte":
                warpperType = "java.lang.Byte";
                break;
            case "char":
                warpperType = "java.lang.Character";
                break;
            case "double":
                warpperType = "java.lang.Double";
                break;
            case "float":
                warpperType = "java.lang.Float";
                break;
            case "int":
                warpperType = "java.lang.Integer";
                break;
            case "long":
                warpperType = "java.lang.Long";
                break;
            case "short":
                warpperType = "java.lang.Short";
                break;
            default:
                break;
        }
        return warpperType;
    }

    public static String wrapperToPrime(String typeName) {
        String warpperType = "";
        switch (typeName) {
            case "boolean":
                warpperType = ".booleanValue()";
                break;
            case "byte":
                warpperType = ".byteValue()";
                break;
            case "char":
                warpperType = ".charValue()";
                break;
            case "double":
                warpperType = ".doubleValue()";
                break;
            case "float":
                warpperType = ".floatValue()";
                break;
            case "int":
                warpperType = ".intValue()";
                break;
            case "long":
                warpperType = ".longValue()";
                break;
            case "short":
                warpperType = ".shortValue()";
                break;
            default:
                break;
        }
        return warpperType;
    }

    public static String getParameterValue(int length) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < length; i++) {
            stringBuilder.append("var" + i);
            if (i != length - 1) {
                stringBuilder.append(",");
            }
        }
        return stringBuilder.toString();
    }

    public static String getParameterSignure(CtMethod ctMethod) {
        StringBuilder methodSignure = new StringBuilder();
        try {
            for (int i = 0; i < ctMethod.getParameterTypes().length; i++) {
                methodSignure.append(ctMethod.getParameterTypes()[i].getName());
                methodSignure.append(" var" + i);
                if (i != ctMethod.getParameterTypes().length - 1) {
                    methodSignure.append(",");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return methodSignure.toString();
    }

    public static String getRealParamtersBody() {
        StringBuilder realParameterBuilder = new StringBuilder();
        realParameterBuilder.append("public  Object[] " + Constants.GET_REAL_PARAMETER + " (Object[] args){");
        realParameterBuilder.append("if (args == null || args.length < 1) {");
        realParameterBuilder.append(" return args;");
        realParameterBuilder.append("}");
        realParameterBuilder.append(" Object[] realParameter = (Object[]) java.lang.reflect.Array.newInstance(args.getClass().getComponentType(), args.length);");
        realParameterBuilder.append("for (int i = 0; i < args.length; i++) {");
        realParameterBuilder.append("if (args[i] instanceof Object[]) {");
        realParameterBuilder.append("realParameter[i] =" + Constants.GET_REAL_PARAMETER + "((Object[]) args[i]);");
        realParameterBuilder.append("} else {");
        realParameterBuilder.append("if (args[i] ==this) {");
        realParameterBuilder.append(" realParameter[i] =this." + ORIGINCLASS + ";");
        realParameterBuilder.append("} else {");
        realParameterBuilder.append(" realParameter[i] = args[i];");
        realParameterBuilder.append(" }");
        realParameterBuilder.append(" }");
        realParameterBuilder.append(" }");
        realParameterBuilder.append("  return realParameter;");
        realParameterBuilder.append(" }");
        return realParameterBuilder.toString();
    }

    public static boolean isInnerClassInModifiedClass(String className, CtClass modifedClass) {
        //only the inner class directly defined in modifedClass
        int index = className.lastIndexOf('$');
        if (index < 0) {
            return false;
        }
        return className.substring(0, index).equals(modifedClass.getName());
    }

    public static CtClass addPatchConstruct(CtClass patchClass, CtClass sourceClass) {
        try {
            CtField originField = new CtField(sourceClass, ORIGINCLASS, patchClass);
            patchClass.addField(originField);
            StringBuilder patchClassConstruct = new StringBuilder();
            patchClassConstruct.append(" public Patch(Object o) {");
            patchClassConstruct.append(ORIGINCLASS + "=(" + sourceClass.getName() + ")o;");
            patchClassConstruct.append("}");
            CtConstructor constructor = CtNewConstructor.make(patchClassConstruct.toString(), patchClass);
            patchClass.addConstructor(constructor);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException();
        }
        return patchClass;
    }

    public
    static boolean isMethodSignureContainPatchClassName(String name, List<String> modifiedClassNameList) {
        for (String classname : modifiedClassNameList) {
            if (name.startsWith(classname)) {
                return true;
            }
        }
        return false;
    }

    public static void printMap(Map<String, ?> memberMappingInfo) {
        if (memberMappingInfo == null) {
            return;
        }
        for (String key : memberMappingInfo.keySet())
            System.out.println("key is   " + key + "  value is    " + memberMappingInfo.get(key));
        System.out.println("");
    }

    public static void printList(List<String> list) {
        if (list == null) {
            return;
        }
        for (String key : list)
            System.out.println("key is   " + key);
        System.out.println("");
    }


    public static String getFullClassNameFromFile(String path) {
        if (path.indexOf("classout") > 0) {
            return path.substring(path.indexOf("classout") + "classout".length() + 1, path.lastIndexOf(".smali")).replace(File.separatorChar, '.');
        }
        if (path.indexOf("main") > 0) {
            return path.substring(path.indexOf("main") + "main".length() + 1, path.lastIndexOf(".class")).replace(File.separatorChar, '.');
        }
        throw new RuntimeException("can not analysis " + path + "  get full class name error!!");
    }

    public static String eradicatReturnType(String name) {
        int blankIndex = name.indexOf(" ");
        if (blankIndex != -1) {
            //method with return type
            return name.substring(blankIndex + 1);
        } else {
            return name;
        }
    }

    public static String getJavaMethodSignure(CtMethod ctMethod) throws NotFoundException {
        StringBuilder methodSignure = new StringBuilder();
        methodSignure.append(ctMethod.getName());
        methodSignure.append("(");
        for (int i = 0; i < ctMethod.getParameterTypes().length; i++) {
            methodSignure.append(ctMethod.getParameterTypes()[i].getName());
            if (i != ctMethod.getParameterTypes().length - 1) {
                methodSignure.append(",");
            }
        }
        methodSignure.append(")");
        return methodSignure.toString();
    }

}
