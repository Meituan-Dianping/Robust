package com.meituan.robust.patch.resources.diff;

import com.meituan.robust.common.FileUtil;
import com.meituan.robust.common.ZipOperation;
import com.meituan.robust.patch.resources.config.RobustResourceConfig;
import com.meituan.robust.patch.resources.diff.apkdiffer.APKDiffer;
import com.meituan.robust.patch.resources.diff.data.APKDiffData;

import java.io.File;
import java.io.IOException;
/**
 * Created by hedingxu on 17/5/31.
 */
public class APKDiffUtils {
    private APKDiffUtils(){

    }
    private static String diffOutputApk = RobustResourceConfig.PATCH_RESOURCES_APK;
    public static void execute(RobustResourceConfig config) throws IOException {

        //1. diff old apk and new apk, generate apk diff data
        APKDiffer apkDiffer = new APKDiffer(config);

        File diffOutDirFile = config.apkDiffOutDir;
        if (diffOutDirFile.exists()) {
            FileUtil.deleteAllFile(diffOutDirFile.getAbsolutePath());
            diffOutDirFile.mkdirs();
        }

        APKDiffData apkDiffData = apkDiffer.diffAPK(config.oldApkFile, config.newApkFile);

        if (apkDiffData.isEmpty()) {
            return;
        }

        if (!diffOutDirFile.exists()) {
            return;
        }

        //2. get apk diff data , write diff data to output's assets dir,adapt windows mac linux
        File apkDiffDataFile = new File(diffOutDirFile, APKDiffData.ROBUST_RESOURCES_DIFF_RELATIVE_PATH);

        boolean writeResult = ApkDiffDataReaderAndWriter.writeDiffData(apkDiffDataFile, apkDiffData);

        if (!writeResult) {
            return;
        }

        //3. zip diff files to diffOutputApk
        File diffOutputApkFile = new File(diffOutDirFile.getParentFile(), diffOutputApk);
        if (diffOutputApkFile.exists()) {
            diffOutputApkFile.delete();
        }

        ZipOperation.zipInputDir(diffOutDirFile, diffOutputApkFile);
    }
}
