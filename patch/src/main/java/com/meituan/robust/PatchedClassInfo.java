package com.meituan.robust;

/**
 * Created by hedex on 16/6/3.
 */
public class PatchedClassInfo {
    public String patchedClassName;
    public String patchClassName;

    public PatchedClassInfo(String patchedClassName, String patchClassName) {
        this.patchedClassName = patchedClassName;
        this.patchClassName = patchClassName;
    }
}
