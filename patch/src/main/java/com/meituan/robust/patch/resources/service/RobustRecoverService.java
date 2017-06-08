package com.meituan.robust.patch.resources.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;

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
        final Context context = getApplicationContext();
        if (intent == null) {
            return;
        }
        String name = intent.getStringExtra(PATCH_NAME_EXTRA);
        String md5 = intent.getStringExtra(PATCH_MD5_EXTRA);
        String path = intent.getStringExtra(PATCH_PATH_EXTRA);
        // TODO: 17/6/7  background thread ?
        boolean result = ApkRecover.recover(context,name,md5,path);
        if (result){

        } else {

        }
    }

    public static void startRobustRecoverService(Context context,String patchName, String patchMd5, String patchPath){
        try {
            Intent intent = new Intent(context, RobustRecoverService.class);
            intent.putExtra(PATCH_NAME_EXTRA, patchName);
            intent.putExtra(PATCH_MD5_EXTRA, patchMd5);
            intent.putExtra(PATCH_PATH_EXTRA, patchPath);
            context.startService(intent);
        }catch (Throwable t){
        }
    }
}
