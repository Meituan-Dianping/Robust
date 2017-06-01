package com.meituan.robust;

import java.io.File;

/**
 * Created by mivanzhang on 15/7/23.
 * 补丁定义
 */
public class Patch implements Cloneable {
    //补丁的编号，补丁的唯一标识符
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name=name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
    //原始补丁文件的路径，推荐放到私有目录
    public String getLocalPath() {
        return localPath + ".jar";
    }

    public void setLocalPath(String localPath) {
        this.localPath = localPath;
    }
    //原始补丁的md5，确保原始补丁文件没有被篡改
    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }

    private String patchesInfoImplClassFullName;
    /**
     * 补丁名称
     */
    private String name;

    /**
     * 补丁的下载url
     */
    private String url;
    /**
     * 补丁本地保存路径
     */
    private String localPath;

    private String tempPath;

    /**
     * 补丁md5值
     */
    private String md5;

    /**
     * app hash值,避免应用内升级导致低版本app的补丁应用到了高版本app上
     */
    private String appHash;

    public boolean isAppliedSuccess() {
        return isAppliedSuccess;
    }

    public void setAppliedSuccess(boolean appliedSuccess) {
        isAppliedSuccess = appliedSuccess;
    }

    /**
     * 补丁是否已经applied success
     */
    private boolean isAppliedSuccess;

    /**
     * 删除文件
     */
    public void delete(String path) {
        File f = new File(path);
        f.delete();
    }

    public String getPatchesInfoImplClassFullName() {
        return patchesInfoImplClassFullName;
    }

    public void setPatchesInfoImplClassFullName(String patchesInfoImplClassFullName) {
        this.patchesInfoImplClassFullName = patchesInfoImplClassFullName;
    }

    public String getAppHash() {
        return appHash;
    }

    public void setAppHash(String appHash) {
        this.appHash = appHash;
    }
    //解密之后的补丁文件，可以直接运行的补丁文件，建议加载之后立刻删除，保证安全性
    public String getTempPath() {
        return tempPath + "_temp" + ".jar";
    }

    public void setTempPath(String tempPath) {
        this.tempPath = tempPath;
    }

    @Override
    public Patch clone() {
        Patch clone = null;
        try {
            clone = (Patch) super.clone();
        } catch (CloneNotSupportedException e) {
//            throw e;
        }
        return clone;
    }
}
