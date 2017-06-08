package com.meituan.robust.patch.resources.diff.data;

import com.meituan.robust.patch.resources.APKStructure;

/**
 * Created by hedingxu on 17/5/31.
 */

public class ResDiffData extends BaseDiffData{
    public ResDiffData() {
        super();
        diffTypeName = APKStructure.Res_Type;
    }
}
