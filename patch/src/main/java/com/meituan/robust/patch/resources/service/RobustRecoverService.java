package com.meituan.robust.patch.resources.service;

import android.app.IntentService;
import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import com.meituan.robust.patch.resources.recover.ApkRecover;
import com.meituan.robust.patch.resources.util.ProcessUtil;

/**
 * Created by hedingxu on 17/6/7.
 */

public class RobustRecoverService extends IntentService {
    private static final String PATCH_NAME_EXTRA = "robust_patch_name_extra";
    private static final String PATCH_MD5_EXTRA = "robust_patch_md5_extra";
    private static final String PATCH_PATH_EXTRA = "robust_patch_path_extra";

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     */
    public RobustRecoverService() {
        super("RobustRecoverService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        final Context context = getApplicationContext();
        if (intent == null) {
            return;
        }
        final String name = intent.getStringExtra(PATCH_NAME_EXTRA);
        final String md5 = intent.getStringExtra(PATCH_MD5_EXTRA);
        final String path = intent.getStringExtra(PATCH_PATH_EXTRA);

        //robust process was killed before finished...
        //29510-29568/? D/Robust: FileUtil.addZipEntry to resources apk 228 : res/layout/vy_massage_select_time_grid_item.xml
        //4935-12025/? I/ActivityManager: Process com.sankuai.meituan:robust (pid 29510) has died
        //set foreground
        try {
            Notification notification = new Notification();
            if (Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN_MR2) {
                startForeground(notificationId, notification);
            } else {
                startForeground(notificationId, notification);
                startService(new Intent(this, InnerService.class));
            }
        } catch (Throwable e) {

        }
        RobustRecoverHelper.getInstance().postRunnable(new Runnable() {
            @Override
            public void run() {
                long currentTime = System.currentTimeMillis();
                boolean result = ApkRecover.recover(context, name, md5, path);
//                Log.w("robust", "current process name :" + ProcessUtil.getProcessName(context));
                //current process name : com.meituan.robust.sample:robust
                Log.w("robust", "ApkRecover spend time: " + (System.currentTimeMillis() - currentTime));
                if (result) {
                    Log.w("robust", "ApkRecover result: " + result);
                } else {
                    result = ApkRecover.recover(context, name, md5, path);
                    Log.w("robust", "ApkRecover2 result: " + result);
                }
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            RobustRecoverHelper.getInstance().killRobustProcessWhenEmpty(context);
        } else {
            //60s后进程自杀
            RobustRecoverHelper.getInstance().postRunnableDelay(new Runnable() {
                @Override
                public void run() {
                    if (ProcessUtil.isRobustProcess(context)) {
                        Log.d("robust", "robust process is empty");
                        Log.d("robust", "kill robust process");
                        ProcessUtil.killSelf();
                    }
                }
            }, 60 * 1000);
        }

    }

    public static void startRobustRecoverService(final Context context, final String patchName, final String patchMd5, final String patchPath) {
        try {
            Intent intent = new Intent(context, RobustRecoverService.class);
            intent.putExtra(PATCH_NAME_EXTRA, patchName);
            intent.putExtra(PATCH_MD5_EXTRA, patchMd5);
            intent.putExtra(PATCH_PATH_EXTRA, patchPath);
            context.startService(intent);
        } catch (Throwable t) {
        }
    }

    //use InnerService
    private static final int notificationId = (int) System.currentTimeMillis();

    public static class InnerService extends Service {

        @Override
        public IBinder onBind(Intent intent) {
            return null;
        }

        @Override
        public void onCreate() {
            super.onCreate();
            try {
                startForeground(notificationId, new Notification());
            } catch (Throwable e) {
            }
            stopSelf();
        }

        @Override
        public void onDestroy() {
            stopForeground(true);
            super.onDestroy();
        }
    }

}
