package com.meituan.robust.resource.util;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Looper;
import android.text.TextUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.lang.reflect.Method;
import java.util.List;

/**
 * Created by hedingxu on 17/3/10.
 */
public class ProcessUtil {
    private static final String ROBUST_PROCESS_NAME = ":robust";

    private static String packageName;

    private static String getCurrentPackageName(Context context) {
        if (TextUtils.isEmpty(packageName)) {
            packageName = context.getPackageName();
        }
        return packageName;
    }

    private static String currentProcessName;

    public static String getCurrentProcessName(Context context) {
        if (TextUtils.isEmpty(currentProcessName)) {
            currentProcessName = getCurrentProcessNameReal(context);
        }
        return currentProcessName;
    }

    public static boolean isRobustProcess(Context context) {
        getCurrentProcessName(context);
        getCurrentPackageName(context);
        if (currentProcessName.startsWith(packageName) && currentProcessName.endsWith(ROBUST_PROCESS_NAME)) {
            return true;
        } else {
            return false;
        }
    }

    public static void killSelf() {
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    public static boolean isMainProcess(Context context) {
        getCurrentProcessName(context);
        getCurrentPackageName(context);
        return currentProcessName != null ? currentProcessName.equalsIgnoreCase(packageName) : true;
    }

    private static String getCurrentProcessNameReal(Context context) {
        String currentProcessName = getCurrentProcessNameByReflect();
        if (TextUtils.isEmpty(currentProcessName)) {
            currentProcessName = getCurrentProcessNameByFile();
        }
        if (TextUtils.isEmpty(currentProcessName)) {
            currentProcessName = getCurrentProcessNameByPid(context);
        }
        return currentProcessName;
    }

    private static String getCurrentProcessNameByReflect() {
        if (Looper.myLooper() == Looper.getMainLooper()){
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
            }
        }
        return null;
    }

    public static String getCurrentProcessNameByPid(Context context) {
        try {
            int pid = android.os.Process.myPid();
            ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            List<ActivityManager.RunningAppProcessInfo> infos = manager.getRunningAppProcesses();
            if (infos != null) {
                for (ActivityManager.RunningAppProcessInfo processInfo : infos) {
                    if (processInfo.pid == pid) {
                        return processInfo.processName;
                    }
                }
            }
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
        return null;
    }

    private static String getCurrentProcessNameByFile() {
        BufferedReader reader = null;
        try {
            final File cmdline = new File("/proc/" + android.os.Process.myPid() + "/cmdline");
            reader = new BufferedReader(new FileReader(cmdline));
            String processNameLine = reader.readLine();
            String pureProcessName = processNameLine.replaceAll("[^0-9a-zA-Z.-_+:]+", "").replace("\n", "");
            return pureProcessName;
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                }
            }
        }
        return "";
    }

}