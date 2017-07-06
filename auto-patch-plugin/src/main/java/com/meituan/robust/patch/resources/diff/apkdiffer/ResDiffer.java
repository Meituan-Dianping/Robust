package com.meituan.robust.patch.resources.diff.apkdiffer;

import com.meituan.robust.resource.APKStructure;
import com.meituan.robust.patch.resources.config.RobustResourceConfig;
import com.meituan.robust.resource.diff.data.ResDiffData;

import java.nio.file.Path;
/**
 * Created by hedingxu on 17/5/31.
 */
public class ResDiffer extends BaseDiffer {
    public ResDiffer(RobustResourceConfig config) {
        super(config);
        diffData = new ResDiffData();
        includePatterns = config.resIncludePatterns;
        excludePatterns = config.resExcludePatterns;
        apkResourceType = APKStructure.Res_Type;
    }

    @Override
    public boolean diffNewFile(Path newFilePath) {
        return super.diffNewFile(newFilePath);
    }

    @Override
    public boolean diffOldFile(Path oldFilePath) {
        return super.diffOldFile(oldFilePath);
    }

    @Override
    protected boolean isNeed(Path filePath) {
        return super.isNeed(filePath);
    }
}
