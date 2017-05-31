package com.meituan.robust;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by mivanzhang on 16/11/3.
 */

public class Constants {

    public static final String ORIGINCLASS = "originClass";
    public static final String MODIFY_ANNOTATION = "com.meituan.robust.patch.annotaion.Modify";
    //    public static final String MODIFY_ANNOTATION = Modify.class.getCanonicalName();
    public static final String ADD_ANNOTATION = "com.meituan.robust.patch.annotaion.Add";
    //    public static final String ADD_ANNOTATION = Add.class.getCanonicalName();
    public static final String LAMBDA_MODIFY = "com.meituan.robust.patch.RobustModify";
    //    public static final String LAMBDA_MODIFY = RobustModify.class.getCanonicalName();

    public static final String PATCH_TEMPLATE_FULL_NAME = "com.meituan.robust.utils.PatchTemplate";


    public static final String ZIP_FILE_NAME = "meituan.jar";
    public static final String PATACH_DEX_NAME = "patch.dex";
    public static final String CLASSES_DEX_NAME = "classes.dex";
    public static final String PATACH_JAR_NAME = "patch.jar";
    public static final String PATCH_SUFFIX = "Patch";
    public static final String PATCH_CONTROL_SUFFIX = "Control";
    public static final String INLINE_PATCH_SUFFIX = "InLinePatch";
    public static final String STATICFLAG = "staticRobust";
    public static final String ROBUST_ASSIST_SUFFIX = "RobustAssist";
    public static final String ROBUST_PUBLIC_SUFFIX = "RobustPublic";
    public static final String GET_REAL_PARAMETER = "getRealParameter";
    public static final String ROBUST_UTILS_FULL_NAME = "com.meituan.robust.utils.EnhancedRobustUtils";

    public static final String ROBUST_GENERATE_DIRECTORY = "outputs/robust";

    //FILE_MD5_PATH is a copy from RobustTransform.FILE_MD5_PATH ,please make sure the two is the same
    public static final String METHOD_MAP_PATH = "/robust/methodsMap.robust";
    public static final String DEFAULT_MAPPING_FILE = "/robust/mapping.txt";

    public static final String SMALI_INVOKE_SUPER_COMMAND = "invoke-super";
    public static final String SMALI_INVOKE_VIRTUAL_COMMAND = "invoke-virtual";

    public static Class ModifyAnnotationClass = null;
    public static Class AddAnnotationClass = null;

    public final static String[] LIB_NAME_ARRAY = {"baksmali-2.1.2.jar", "smali-2.1.2.jar", "dx.jar"};
    public static final String PACKNAME_END = ";";
    public final static String PRIMITIVE_TYPE = "ZCBSIJFDV";
    public final static String ARRAY_TYPE = "[";
    public final static char OBJECT_TYPE = 'L';
    public static final String PACKNAME_START = String.valueOf(OBJECT_TYPE);
    public static final Boolean OBSCURE = true;
    //    public static final Boolean OBSCURE = false;
    //    public static final Boolean isLogging = false;
    public static boolean isLogging = true;

    public static final String PATCH_PACKAGENAME = "com.meituan.robust.patch";
    public static final Set RFileClassSet = new HashSet();
    public final static String ROBUST_XML = "robust.xml";

    static {
        RFileClassSet.add("R$array");
        RFileClassSet.add("R$xml");
        RFileClassSet.add("R$styleable");
        RFileClassSet.add("R$style");
        RFileClassSet.add("R$string");
        RFileClassSet.add("R$raw");
        RFileClassSet.add("R$menu");
        RFileClassSet.add("R$layout");
        RFileClassSet.add("R$integer");
        RFileClassSet.add("R$id");
        RFileClassSet.add("R$drawable");
        RFileClassSet.add("R$dimen");
        RFileClassSet.add("R$color");
        RFileClassSet.add("R$bool");
        RFileClassSet.add("R$attr");
        RFileClassSet.add("R$anim");
    }

    //=========================RobustTransForm========================
    //=========================RobustTransForm========================
    public static final String CONSTRUCTOR = "Constructor";
    public static final String LANG_VOID = "java.lang.Void";
    public static final String VOID = "void";
    public static final String LANG_BOOLEAN = "java.lang.Boolean";
    public static final String BOOLEAN = "boolean";
    public static final String LANG_INT = "java.lang.Integer";
    public static final String INT = "int";
    public static final String LANG_LONG = "java.lang.Long";
    public static final String LONG = "long";
    public static final String LANG_DOUBLE = "java.lang.Double";
    public static final String DOUBLE = "double";
    public static final String LANG_FLOAT = "java.lang.Float";
    public static final String FLOAT = "float";
    public static final String LANG_SHORT = "java.lang.Short";
    public static final String SHORT = "short";
    public static final String LANG_BYTE = "java.lang.Byte";
    public static final String BYTE = "byte";
    public static final String LANG_CHARACTER = "Character";
    public static final String CHAR = "char";

    public static final String METHOD_MAP_OUT_PATH = "/outputs/robust/methodsMap.robust";
    public static final String INTERFACE_NAME = "com.meituan.robust.ChangeQuickRedirect";
    public static final String INSERT_FIELD_NAME = "changeQuickRedirect";
    public static final List<String> NO_NEED_REFLECT_CLASS = Arrays.asList("android.os.Bundle","android.os.BaseBundle");

    //robust apk hash : apk's unique id
    public static final String ROBUST_APK_HASH_FILE_NAME = "robust.apkhash";
    public static final String ASPECTJ_AROUND_CLASS = "org.aspectj.runtime.internal.AroundClosure";
    public static final String PATCH_EXECUTE = "patch execute ,other extension will be ignore ";



}
