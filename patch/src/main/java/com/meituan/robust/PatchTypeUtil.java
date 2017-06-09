package com.meituan.robust;

import com.meituan.robust.common.ResourceConstant;

import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Created by hedingxu on 17/6/8.
 * 1.patches dex + resources
 * 2.patches dex
 * 3.patches resources
 */

public class PatchTypeUtil {
    private PatchTypeUtil() {

    }

    private static final int PATCH_NONE_TYPE = 0;
    private static final int PATCH_DEX_TYPE = 1;
    private static final int PATCH_RESOURCE_TYPE = 2;
    private static final int PATCH_DEX_AND_RESOURCE_TYPE = 3;

    static int getPatchType(Patch p) {
        boolean hasDex = false;
        boolean hasResource = false;
        // TODO: 17/6/8
        ZipFile patchZipFile = null;
        try {
            patchZipFile = new ZipFile(p.getTempPath());
        } catch (IOException e) {
            e.printStackTrace();
        }


        ZipEntry dexEntry = patchZipFile.getEntry("classes.dex");
        if (dexEntry == null) {
            hasDex = false;
        } else {
            hasDex = true;
        }

        ZipEntry resourceEntry = patchZipFile.getEntry(ResourceConstant.ROBUST_RESOURCES_DIFF_RELATIVE_PATH);
        if (resourceEntry == null) {
            hasResource = false;
        } else {
            hasResource = true;
        }

        try {
            if (null != patchZipFile) {
                patchZipFile.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (hasDex && hasResource) {
            return PATCH_DEX_AND_RESOURCE_TYPE;
        } else {
            if (hasDex) {
                return PATCH_DEX_TYPE;
            }
            if (hasResource) {
                return PATCH_RESOURCE_TYPE;
            }
        }
        return PATCH_NONE_TYPE;
    }

    static boolean isDexType(int patchType) {
        if (patchType == PATCH_DEX_TYPE) {
            return true;
        } else {
            return false;
        }
    }

    static boolean isResourceType(int patchType) {
        if (patchType == PATCH_RESOURCE_TYPE) {
            return true;
        } else {
            return false;
        }
    }

    static boolean isDexAndResourceType(int patchType) {
        if (patchType == PATCH_DEX_AND_RESOURCE_TYPE) {
            return true;
        } else {
            return false;
        }
    }
}
