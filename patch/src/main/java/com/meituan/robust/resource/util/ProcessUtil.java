package com.meituan.robust.resource.util;

import android.content.Context;
import android.text.TextUtils;

import java.lang.reflect.Method;

/**
 * Created by hedingxu on 17/3/10.
 */
public class ProcessUtil {
    private static final String ROBUST_PROCESS_NAME = ":robust";

    private static String currentPackageName;

    private static String getCurrentPackageName(Context context) {
        if (TextUtils.isEmpty(currentPackageName)) {
            currentPackageName = context.getPackageName();
        }
        return currentPackageName;
    }

    private static String currentProcessName;

    public static String getCurrentProcessName() {
        if (TextUtils.isEmpty(currentProcessName)) {
            currentProcessName = getCurrentProcessNameByReflect();
        }
        return currentProcessName;
    }

    public static boolean isRobustProcess(Context context) {
        getCurrentProcessName();
        getCurrentPackageName(context);
        if (currentProcessName.startsWith(currentPackageName) && currentProcessName.endsWith(ROBUST_PROCESS_NAME)) {
            return true;
        } else {
            return false;
        }
    }

    public static void killSelf() {
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    public static boolean isMainProcess(Context context) {
        getCurrentProcessName();
        getCurrentPackageName(context);
        return currentProcessName != null ? currentProcessName.equalsIgnoreCase(currentPackageName) : true;
    }

    private static String getCurrentProcessNameByReflect() {
        try {
            Class clazz = Class.forName("android.app.ActivityThread");
            Method tCurrentActivityThreadMethod = clazz.getDeclaredMethod("currentActivityThread");
            tCurrentActivityThreadMethod.setAccessible(true);
            Object tCurrentActivityThread = tCurrentActivityThreadMethod.invoke(null);

            Method tGetProcessNameMethod = clazz.getDeclaredMethod("getProcessName");
            tGetProcessNameMethod.setAccessible(true);
            return (String) tGetProcessNameMethod.invoke(tCurrentActivityThread);
        } catch (Throwable e) {
            e.printStackTrace();
            return null;
        }
    }

}
