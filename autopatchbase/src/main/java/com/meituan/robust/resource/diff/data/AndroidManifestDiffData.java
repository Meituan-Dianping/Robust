package com.meituan.robust.resource.diff.data;

import com.meituan.robust.resource.APKStructure;

/**
 * Created by hedingxu on 17/5/31.
 */

public class AndroidManifestDiffData extends BaseDiffData {
    public AndroidManifestDiffData() {
        super();
        diffTypeName = APKStructure.AndroidManifest_Type;
    }
}
