package com.meituan.robust.autopatch;

import com.meituan.robust.Constants;

import java.util.HashMap;

/**
 * Created by mivanzhang on 17/2/10.
 * <p>
 * manager patches' name ,inline patch name
 */

public class NameManger {
    private static NameManger nameManger;
    private HashMap<String, String> patchNameMap = new HashMap();

    private NameManger() {

    }

    public static void init() {
        nameManger = new NameManger();
    }

    public static NameManger getInstance() {
        if (nameManger == null) {
            nameManger = new NameManger();
        }
        return nameManger;
    }

    public HashMap<String, String> getPatchNameMap() {
        return patchNameMap;
    }

    /***
     * @param className java class name java.lang.object
     * @return patchname.object
     */
    public String getPatchName(String className) {
        String patchName = getPatchNamWithoutRecord(className);
        patchNameMap.put(patchName, className);
        return patchName;
    }

    public String getInlinePatchName(String className) {
        String patchName = getInlinePatchNameWithoutRecord(className);
        patchNameMap.put(patchName, className);
        return patchName;
    }

    public String getInlinePatchNameWithoutRecord(String className) {
        String patchName = Config.patchPackageName + "." + className.substring(className.lastIndexOf(".") + 1) + Constants.INLINE_PATCH_SUFFIX;
        return patchName;
    }

    public String getPatchNamWithoutRecord(String className) {
        String patchName = Config.patchPackageName + "." + className.substring(className.lastIndexOf(".") + 1) + Constants.PATCH_SUFFIX;
        return patchName;
    }

    public String getPatchControlName(String simpleClassName) {
        return Config.patchPackageName + "." + simpleClassName + Constants.PATCH_SUFFIX + Constants.PATCH_CONTROL_SUFFIX;
    }

    public String getAssistClassName(String s) {
        patchNameMap.put(s + Constants.ROBUST_ASSIST_SUFFIX, s + Constants.ROBUST_ASSIST_SUFFIX);
        return s + Constants.ROBUST_ASSIST_SUFFIX;
    }
}
