package com.meituan.robust.patch.resources.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.meituan.robust.patch.resources.recover.ApkRecover;

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
        Log.w("Recover", "intent onHandleIntent");
        final Context context = getApplicationContext();
        if (intent == null) {
            return;
        }
        final String name = intent.getStringExtra(PATCH_NAME_EXTRA);
        final String md5 = intent.getStringExtra(PATCH_MD5_EXTRA);
        final String path = intent.getStringExtra(PATCH_PATH_EXTRA);

        RobustRecoverHelper.getInstance().postRunnable(new Runnable() {
            @Override
            public void run() {
                boolean result = ApkRecover.recover(context, name, md5, path);
                Log.w("Recover", "result :" + result);
                if (result) {

                } else {

                }
            }
        });

    }

    public static void startRobustRecoverService(final Context context, final String patchName, final String patchMd5, final String patchPath) {

        try {

            Intent intent = new Intent(context, RobustRecoverService.class);
            intent.putExtra(PATCH_NAME_EXTRA, patchName);
            intent.putExtra(PATCH_MD5_EXTRA, patchMd5);
            intent.putExtra(PATCH_PATH_EXTRA, patchPath);
            context.startService(intent);

//            boolean isUiThread = Looper.getMainLooper() == Looper.myLooper();
//            if (isUiThread || true) {
//                Intent intent = new Intent(context, RobustRecoverService.class);
//                intent.putExtra(PATCH_NAME_EXTRA, patchName);
//                intent.putExtra(PATCH_MD5_EXTRA, patchMd5);
//                intent.putExtra(PATCH_PATH_EXTRA, patchPath);
//                //// TODO: 17/6/10 挪到patch进程
//                boolean result = ApkRecover.recover(context, patchName, patchMd5, patchPath);
//                context.startService(intent);
//            } else {
//                Handler handler = new Handler(Looper.getMainLooper());
//                handler.post(new Runnable() {
//                    @Override
//                    public void run() {
//                        Intent intent = new Intent(context, RobustRecoverService.class);
//                        intent.putExtra(PATCH_NAME_EXTRA, patchName);
//                        intent.putExtra(PATCH_MD5_EXTRA, patchMd5);
//                        intent.putExtra(PATCH_PATH_EXTRA, patchPath);
//                        //// TODO: 17/6/10 挪到patch进程
//                        boolean result = ApkRecover.recover(context, patchName, patchMd5, patchPath);
//                        context.startService(intent);
//                    }
//                });
//
//            }

        } catch (Throwable t) {
        }
    }
}
