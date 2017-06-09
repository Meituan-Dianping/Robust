package com.meituan.robust.patch.resources.config;

import com.meituan.robust.autopatch.Config;

import java.util.HashSet;

/**
 * Created by hedex on 17/6/2.
 * 存储robust.xml的资源部分的相关信息
 */
public class RobustXmlResourceInfo {
    public String oldApkPath;
    public String newApkPath;
    public String robustOutputDirPath = Config.robustGenerateDirectory;

    public HashSet<String> resIncludeStrings;
    public HashSet<String> resExcludeStrings;

    public HashSet<String> libIncludeStrings;
    public HashSet<String> libExcludeStrings;

    public HashSet<String> assetsIncludeStrings;
    public HashSet<String> assetsExcludeStrings;
    public int bigFileSizeAtLeast;
}
