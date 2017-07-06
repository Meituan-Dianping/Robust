package com.meituan.robust.resource.util;

import android.app.ActivityManager;
import android.content.Context;
import android.text.TextUtils;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.UUID;

/**
 * Created by hedingxu on 17/3/10.
 */
public class ProcessUtil {
    private static final String ROBUST_PROCESS_NAME = ":robust";
    private static String processName = "";
    private static Boolean isMainProcess = false;

    public static boolean isRobustProcess(Context context) {
        String pkgName = context.getPackageName();
        String processName = getCurrentProcessName(context);
        if (processName == null || processName.length() == 0) {
            processName = "";
        }
        if (processName.startsWith(pkgName) && processName.endsWith(ROBUST_PROCESS_NAME)) {
            return true;
        } else {
            return false;
        }
    }

    public static void killAllProcessButMainProcess(Context context) {
        if (isMainProcess(context)) {
            final ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            if (am == null) {
                return;
            }
            for (ActivityManager.RunningAppProcessInfo runningAppProcessInfo : am.getRunningAppProcesses()) {
                //is same uid ,but different pid, kill
                if (runningAppProcessInfo.uid == android.os.Process.myUid() && runningAppProcessInfo.pid != android.os.Process.myPid()) {
                    android.os.Process.killProcess(runningAppProcessInfo.pid);
                }
            }
        }
    }

    public static void killSelf() {
        android.os.Process.killProcess(android.os.Process.myPid());
    }


    private static String getProcessName(Context context) {
        String processNameStr = getProcessNameByPid(context);
        if (TextUtils.isEmpty(processNameStr)) {
            processNameStr = getProcessNameByFile();
        }

        if (TextUtils.isEmpty(processNameStr)) {
            return UUID.randomUUID().toString();
        } else {
            return processNameStr;
        }
    }

    private static String getProcessNameByPid(Context context) {
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
        return "";
    }

    private static String getProcessNameByFile() {
        BufferedReader cmdlineReader = null;
        try {
            cmdlineReader = new BufferedReader(new InputStreamReader(
                    new FileInputStream("/proc/" + android.os.Process.myPid() + "/cmdline"), "iso-8859-1"));
            int c;
            StringBuilder processName = new StringBuilder();
            while ((c = cmdlineReader.read()) > 0) {
                processName.append((char) c);
            }
            return processName.toString();
        } catch (UnsupportedEncodingException e) {
        } catch (FileNotFoundException e) {
        } catch (Throwable throwable) {
        } finally {
            if (cmdlineReader != null) {
                try {
                    cmdlineReader.close();
                } catch (IOException e) {
                }
            }
        }
        return null;
    }

    public static String getCurrentProcessName(Context c) {
        if (TextUtils.isEmpty(processName)) {
            processName = getProcessName(c);
        }
        return processName;
    }

    public static boolean isMainProcess(Context context) {
        if (null == isMainProcess) {
            isMainProcess = estimateMainProcess(context);
        }
        return isMainProcess;
    }

    private static boolean estimateMainProcess(Context context) {
        try {
            ActivityManager am = ((ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE));
            if (am == null) {
                return false;
            }
            List<ActivityManager.RunningAppProcessInfo> processInfos = am.getRunningAppProcesses();
            if (processInfos == null) {
                return false;
            }
            String mainProcessName = context.getPackageName();
            int myPid = android.os.Process.myPid();
            for (ActivityManager.RunningAppProcessInfo info : processInfos) {
                if (info.pid == myPid && mainProcessName.equals(info.processName)) {
                    return true;
                }
            }
            return false;
        } catch (Throwable throwable) {
            return false;
        }
    }

}
