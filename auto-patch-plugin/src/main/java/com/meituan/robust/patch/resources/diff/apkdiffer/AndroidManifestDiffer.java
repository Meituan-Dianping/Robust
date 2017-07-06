package com.meituan.robust.patch.resources.diff.apkdiffer;

import com.meituan.robust.resource.APKStructure;
import com.meituan.robust.patch.resources.config.RobustResourceConfig;

import java.nio.file.Path;

/**
 * Created by hedingxu on 17/5/31.
 */

public class AndroidManifestDiffer extends BaseDiffer {
    public AndroidManifestDiffer(RobustResourceConfig config) {
        super(config);
        apkResourceType = APKStructure.AndroidManifest_Type;
    }

    @Override
    public boolean diffNewFile(Path filePath) {
        return false;
    }

    @Override
    public boolean diffOldFile(Path filePath) {
        return false;
    }

    @Override
    protected boolean isNeed(Path filePath) {
        return false;
    }
}
