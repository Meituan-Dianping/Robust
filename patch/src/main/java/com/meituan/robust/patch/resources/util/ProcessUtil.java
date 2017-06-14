package com.meituan.robust.patch.resources.util;

import android.app.ActivityManager;
import android.content.Context;
import android.text.TextUtils;

import java.io.FileInputStream;
/**
 * Created by hedingxu on 17/3/10.
 */
public class ProcessUtil {
    private static final String ROBUST_PROCESS_NAME = ":robust";
    private static String processName;

    public static boolean isRobustProcess(Context context) {
        String pkgName = context.getPackageName();
        String processName = getProcessName(context);
        if (processName == null || processName.length() == 0) {
            processName = "";
        }
        if (processName.startsWith(pkgName) && processName.endsWith(ROBUST_PROCESS_NAME)) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean isMainProcess(Context context) {
        String pkgName = context.getPackageName();
        String processName = getProcessName(context);
        if (processName == null || processName.length() == 0) {
            processName = "";
        }

        return pkgName.equals(processName);
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

    public static String getProcessName(final Context context) {
        if (!TextUtils.isEmpty(processName)) {
            return processName;
        }
        processName = getProcessNameInternal(context);
        return processName;
    }


    private static String getProcessNameInternal(final Context context) {
        int myPid = android.os.Process.myPid();

        if (context == null || myPid <= 0) {
            return "";
        }

        ActivityManager.RunningAppProcessInfo myProcess = null;
        ActivityManager activityManager =
                (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);

        try {
            for (ActivityManager.RunningAppProcessInfo process : activityManager.getRunningAppProcesses()) {
                if (process.pid == myPid) {
                    myProcess = process;
                    break;
                }
            }
        } catch (Exception e) {
        }

        if (myProcess != null) {
            return myProcess.processName;
        }

        byte[] b = new byte[128];
        FileInputStream in = null;
        try {
            in = new FileInputStream("/proc/" + myPid + "/cmdline");
            int len = in.read(b);
            if (len > 0) {
                for (int i = 0; i < len; i++) { // lots of '0' in tail , remove them
                    if (b[i] > 128 || b[i] <= 0) {
                        len = i;
                        break;
                    }
                }
                return new String(b, 0, len);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (Exception e) {
            }
        }

        return "";
    }

}
