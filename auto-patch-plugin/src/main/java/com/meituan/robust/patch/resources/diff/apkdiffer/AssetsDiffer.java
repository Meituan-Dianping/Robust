package com.meituan.robust.patch.resources.diff.apkdiffer;

import com.meituan.robust.patch.resources.APKStructure;
import com.meituan.robust.patch.resources.config.RobustResourceConfig;
import com.meituan.robust.patch.resources.diff.data.APKDiffData;
import com.meituan.robust.patch.resources.diff.data.AssetsDiffData;

import java.nio.file.Path;

/**
 * Created by hedingxu on 17/5/31.
 */

public class AssetsDiffer extends BaseDiffer {

    public AssetsDiffer(RobustResourceConfig config) {
        super(config);
        diffData = new AssetsDiffData();
        includePatterns = config.assetsIncludePatterns;
        excludePatterns = config.assetsExcludePatterns;
        apkResourceType = APKStructure.Assets_Type;
    }


    @Override
    public boolean diffNewFile(Path newFilePath) {
        if (isRobustApkHashFile(newFilePath)) {
            return false;
        }

        if (isRobustApkDataDiffFile(newFilePath)){
            return false;
        }

        if (!isNeed(newFilePath)) {
            return false;
        }

        return super.diffNewFile(newFilePath);
    }

    private boolean isRobustApkHashFile(Path filePath) {
        if (filePath.equals(APKStructure.Assets_Type + "/robust.apkhash")) {
            return true;
        }
        return false;
    }

    private boolean isRobustApkDataDiffFile(Path filePath) {
        if (filePath.equals(APKDiffData.ROBUST_RESOURCES_DIFF_RELATIVE_PATH)) {
            return true;
        }
        return false;
    }

    @Override
    public boolean diffOldFile(Path oldFilePath) {
        if (isRobustApkHashFile(oldFilePath)) {
            return false;
        }

        if (isRobustApkDataDiffFile(oldFilePath)){
            return false;
        }

        if (!isNeed(oldFilePath)) {
            return false;
        }

        return super.diffOldFile(oldFilePath);
    }

    @Override
    protected boolean isNeed(Path filePath) {
        return super.isNeed(filePath);
    }

}
