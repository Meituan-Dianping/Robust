package com.meituan.robust.patch.resources.diff.apkdiffer;

import com.meituan.robust.patch.resources.APKStructure;
import com.meituan.robust.patch.resources.config.RobustResourceConfig;

import java.nio.file.Path;

/**
 * Created by hedingxu on 17/5/31.
 */

public class DexDiffer extends BaseDiffer {
    public DexDiffer(RobustResourceConfig config){
        super(config);
        apkResourceType = APKStructure.Dex_Type;
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
    public boolean isNeed(Path filePath) {
        return false;
    }


}
