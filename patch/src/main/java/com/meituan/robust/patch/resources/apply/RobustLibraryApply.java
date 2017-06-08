package com.meituan.robust.patch.resources.apply;

import android.os.Build;

import java.io.File;
import java.lang.reflect.Array;
import java.util.List;

import dalvik.system.PathClassLoader;

/**
 * Created by hedingxu on 17/6/7.
 */

public class RobustLibraryApply {
    //修复So
    public static synchronized boolean addNativeLibraryDirectories(PathClassLoader dexClassLoader, File robustNativeLibraryDirectories) throws Exception {
        if (null == dexClassLoader) {
            dexClassLoader = getClassLoader();
        }
        Object fieldPathList = RobustLibraryApply.getFieldPathList(getClassLoader());
        if (Build.VERSION.SDK_INT > 22) {
            ((List) RobustLibraryHook.getFieldValue(fieldPathList.getClass(), fieldPathList, "nativeLibraryDirectories")).add(0, robustNativeLibraryDirectories);
            Object nativeLibraryPathElementsField = RobustLibraryHook.getFieldValue(fieldPathList.getClass(), fieldPathList, "nativeLibraryPathElements");
            Object fieldPathListValue = getFieldPathList((Object) dexClassLoader);
            RobustLibraryHook.setField(fieldPathList.getClass(), fieldPathList, "nativeLibraryPathElements", mergeArrary(nativeLibraryPathElementsField, RobustLibraryHook.getFieldValue(fieldPathListValue.getClass(), fieldPathListValue, "nativeLibraryPathElements")));
        } else {
            //BaseDexClassLoader#pathList(DexPathList)
            ///** list of native library directory elements */
            //private final File[] nativeLibraryDirectories;
            File[] fileArray = (File[]) RobustLibraryHook.getFieldValueWithTryCatch(fieldPathList.getClass(), fieldPathList, "nativeLibraryDirectories");
            int length = fileArray.length;
            File[] newFileArray = new File[(length + 1)];
            newFileArray[0] = robustNativeLibraryDirectories;
            System.arraycopy(fileArray, 0, newFileArray, 1, length);
            RobustLibraryHook.setField(fieldPathList.getClass(), fieldPathList, "nativeLibraryDirectories", newFileArray);
        }
        return true;
    }

    private static Object getFieldPathList(Object obj) throws Exception {
        return RobustLibraryHook.getFieldValue(Class.forName("dalvik.system.BaseDexClassLoader"), obj, "pathList");
    }

    private static PathClassLoader getClassLoader() {
        return (PathClassLoader) RobustLibraryApply.class.getClassLoader();
    }

    private static Object mergeArrary(Object array1, Object array2) {
        Class componentType = array1.getClass().getComponentType();
        int length1 = Array.getLength(array1);
        int length2 = Array.getLength(array2) + length1;
        Object newInstance = Array.newInstance(componentType, length2);
        for (int i = 0; i < length2; i++) {
            if (i < length1) {
                Array.set(newInstance, i, Array.get(array1, i));
            } else {
                Array.set(newInstance, i, Array.get(array2, i - length1));
            }
        }
        return newInstance;
    }
}
