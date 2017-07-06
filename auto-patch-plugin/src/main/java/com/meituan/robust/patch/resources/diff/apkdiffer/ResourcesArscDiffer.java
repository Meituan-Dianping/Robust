package com.meituan.robust.patch.resources.diff.apkdiffer;

import com.meituan.robust.resource.APKStructure;
import com.meituan.robust.patch.resources.config.RobustResourceConfig;
import com.meituan.robust.resource.diff.data.ResourcesArscDiffData;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.regex.Pattern;

/**
 * Created by hedingxu on 17/5/31.
 */

public class ResourcesArscDiffer extends BaseDiffer {

    public ResourcesArscDiffer(RobustResourceConfig config) {
        super(config);
        diffData = new ResourcesArscDiffData();
        Pattern pattern = Pattern.compile(APKStructure.ResourcesArsc_Type);
        HashSet<Pattern> patterns = new HashSet<>();
        patterns.add(pattern);
        includePatterns = patterns;
        excludePatterns = null;
        apkResourceType = APKStructure.ResourcesArsc_Type;
    }

    @Override
    public boolean diffNewFile(Path newFilePath) {
        if (!isNeed(newFilePath)) {
            return false;
        }

        return super.diffNewFile(newFilePath);
    }

    @Override
    public boolean diffOldFile(Path oldFilePath) {
        if (!isNeed(oldFilePath)) {
            return false;
        }

        return super.diffOldFile(oldFilePath);
    }

    @Override
    protected boolean isNeed(Path filePath) {
        return true;
    }


}
