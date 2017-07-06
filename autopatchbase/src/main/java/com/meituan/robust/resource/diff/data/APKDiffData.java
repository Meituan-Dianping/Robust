package com.meituan.robust.resource.diff.data;

import com.meituan.robust.common.ResourceConstant;

/**
 * Created by hedingxu on 17/5/31.
 */

public class APKDiffData extends BaseDiffData {
    public long oldResourcesArscCrc;
    public static final String ROBUST_RESOURCES_DIFF_RELATIVE_PATH = ResourceConstant.ROBUST_RESOURCES_DIFF_RELATIVE_PATH;

    public APKDiffData() {
        diffTypeName = "apk";
    }

    public void mergeDiffData(BaseDiffData diffData) {
        this.addSet.addAll(diffData.addSet);
        this.delSet.addAll(diffData.delSet);
        this.modSet.addAll(diffData.modSet);
        this.diffModSet.addAll(diffData.diffModSet);
    }

    public LibDiffData getLibDiffData() {
        BaseDiffData diffData = new LibDiffData();
        return (LibDiffData) getDiffData(diffData);
    }

    public ResDiffData getResDiffData() {
        BaseDiffData diffData = new ResDiffData();
        return (ResDiffData) getDiffData(diffData);
    }

    public AssetsDiffData getAssetsDiffData() {
        BaseDiffData diffData = new AssetsDiffData();
        return (AssetsDiffData) getDiffData(diffData);
    }

    public DexDiffData getDexDiffData() {
        BaseDiffData diffData = new DexDiffData();
        return (DexDiffData) getDiffData(diffData);
    }

    public com.meituan.robust.resource.diff.data.METAINFDiffData getMETAINFDiffData() {
        BaseDiffData diffData = new com.meituan.robust.resource.diff.data.METAINFDiffData();
        return (com.meituan.robust.resource.diff.data.METAINFDiffData) getDiffData(diffData);
    }

    public com.meituan.robust.resource.diff.data.ResourcesArscDiffData getResourcesArscDiffData() {
        BaseDiffData diffData = new com.meituan.robust.resource.diff.data.ResourcesArscDiffData();
        return (com.meituan.robust.resource.diff.data.ResourcesArscDiffData) getDiffData(diffData);
    }

    public AndroidManifestDiffData getAndroidManifestDiffData() {
        BaseDiffData diffData = new AndroidManifestDiffData();
        return (AndroidManifestDiffData) getDiffData(diffData);
    }

    private BaseDiffData getDiffData(BaseDiffData diffData) {
        for (DataUnit dataUint : this.addSet) {
            if (dataUint.name.startsWith(diffData.diffTypeName)) {
                diffData.addSet.add(dataUint);
            }
        }

        for (DataUnit dataUint : this.delSet) {
            if (dataUint.name.startsWith(diffData.diffTypeName)) {
                diffData.delSet.add(dataUint);
            }
        }

        for (DataUnit dataUint : this.modSet) {
            if (dataUint.name.startsWith(diffData.diffTypeName)) {
                diffData.modSet.add(dataUint);
            }
        }

        for (DataUnit dataUint : this.diffModSet) {
            if (dataUint.name.startsWith(diffData.diffTypeName)) {
                diffData.diffModSet.add(dataUint);
            }
        }

        return diffData;
    }

    public boolean isContains(String name){
        for (DataUnit dataUint : this.addSet) {
            if (dataUint.name.equalsIgnoreCase(name)) {
                return true;
            }
        }

        for (DataUnit dataUint : this.delSet) {
            if (dataUint.name.equalsIgnoreCase(name)) {
                return true;
            }
        }

        for (DataUnit dataUint : this.modSet) {
            if (dataUint.name.equalsIgnoreCase(name)) {
                return true;
            }
        }

        for (DataUnit dataUint : this.diffModSet) {
            if (dataUint.name.equalsIgnoreCase(name)) {
                return true;
            }
        }

        return false;
    }
}
