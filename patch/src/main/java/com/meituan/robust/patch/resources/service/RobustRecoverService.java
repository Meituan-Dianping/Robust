package com.meituan.robust.patch.resources.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
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

        RobustRecoverHelper.getInstance().postRunnable(new Runnable() {
            @Override
            public void run() {
                boolean result = ApkRecover.recover(context, name, md5, path);
                Log.w("robust", "current process name :" + ProcessUtil.getProcessName(context));
                //current process name : com.meituan.robust.sample:robust
                Log.w("robust", "ApkRecover result: " + result);
                if (result) {

                } else {
                    result = ApkRecover.recover(context, name, md5, path);
                    Log.w("robust", "ApkRecover2 result: " + result);
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
        } catch (Throwable t) {
        }
    }
}
