package com.meituan.robust.patch.resources.diff.data;

import com.meituan.robust.patch.resources.APKStructure;

/**
 * Created by hedingxu on 17/5/31.
 */

public class LibDiffData extends BaseDiffData{
    public LibDiffData() {
        super();
        diffTypeName = APKStructure.Lib_Type;
    }
}
