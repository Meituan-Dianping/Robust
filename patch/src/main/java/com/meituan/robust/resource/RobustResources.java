package com.meituan.robust.resource;

import android.content.Context;
import android.text.TextUtils;

import com.meituan.robust.common.ResourceConstant;
import com.meituan.robust.resource.apply.RobustLibraryApply;
import com.meituan.robust.resource.apply.RobustResourceApply;
import com.meituan.robust.resource.recover.ApkRecover;

import java.io.File;

/**
 * Created by hedingxu on 17/3/10.
 */

public class RobustResources {

    private RobustResources() {
    }

    //only execute one times
    public static boolean resFix(Context context, String patchName, String patchMd5/*, String patchPath*/) {
        if (null == context || TextUtils.isEmpty(patchName) /*|| TextUtils.isEmpty(patchMd5) || TextUtils.isEmpty(patchPath)*/) {
            return false;
        }
        context = context.getApplicationContext();

        //check shared preference robust resource switch
        if (!RobustResourcesSwitch.getResourcesSwitch(context)) {
            return false;
        }

        if (getRobustAssetsResource(context)) {
            return true;
        }

//        // get a patch apk and getRobustAssetsResource valid
//        if (!ApkRecover.isRecovered(context, patchName, patchMd5)) {
//            // 开启一个进程去做
//            // 做完了自动退出
//            // base apk + resources_patch -> resources.apk
//            RobustRecoverService.startRobustRecoverService(context, patchName, patchMd5, patchPath);//call ApkRecover.recover
////            ApkRecover.recover(context, patchName, patchMd5, patchPath);
//            return false;
//        }
        String resourcesApkPath = ApkRecover.getRobustResourcesApkPath(context, patchName, patchMd5);
        File resourcesApk = new File(resourcesApkPath);
        if (!resourcesApk.exists()) {
            //resourcesApk not found
            return false;
        }

        boolean resourceCheck = getRobustAssetsResource(context);
        if (resourceCheck) {
            return true;
        }

        boolean applyResult = false;
        try {
            applyResult = RobustResourceApply.patchExistingResourcesOnUiThread(context, resourcesApk.getAbsolutePath());
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }

        return applyResult;
    }

    public static boolean libFix(Context context, String patchName, String patchMd5) {
        if (null == context || TextUtils.isEmpty(patchName) || TextUtils.isEmpty(patchMd5)) {
            return false;
        }
        context = context.getApplicationContext();

        //check shared preference robust resource switch
        if (!RobustResourcesSwitch.getResourcesSwitch(context)) {
            return false;
        }

        File libDirFile = ApkRecover.getRobustResourcesMergeDirLibFile(context, patchName, patchMd5);
        if (null == libDirFile) {
            return false;
        }
        if (!libDirFile.exists() || !libDirFile.isDirectory()) {
            return false;
        }
        String[] names = libDirFile.list();
        boolean isHasSoFile = false;
        for (String name : names) {
            if (name.endsWith(".so")) {
                isHasSoFile = true;
            }
        }

        if (!isHasSoFile) {
            return false;
        }

        boolean result = false;
        try {
            result = RobustLibraryApply.addNativeLibraryDirectories(null, libDirFile);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    private static boolean getRobustAssetsResource(Context context) {
        try {
            context.getAssets().open(ResourceConstant.ROBUST_RESOURCES_DIFF);
        } catch (Throwable e) {
            return false;
        }
        return true;
    }

}
