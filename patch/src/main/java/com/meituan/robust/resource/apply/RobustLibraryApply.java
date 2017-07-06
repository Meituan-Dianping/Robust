package com.meituan.robust.resource.apply;

import android.os.Build;

import java.io.File;
import java.lang.reflect.Array;
import java.util.List;

import dalvik.system.PathClassLoader;

/**
 * Created by hedingxu on 17/6/7.
 */

public class RobustLibraryApply {
    public static synchronized boolean addNativeLibraryDirectories(PathClassLoader dexClassLoader, File robustNativeLibraryDirectories) throws Exception {
        if (null == dexClassLoader) {
            dexClassLoader = getClassLoader();
        }
        Object fieldPathList = RobustLibraryApply.getFieldPathList(getClassLoader());
        if (Build.VERSION.SDK_INT > 22) {
            ((List) RobustLibraryReflect.getFieldValue(fieldPathList.getClass(), fieldPathList, "nativeLibraryDirectories")).add(0, robustNativeLibraryDirectories);
            Object nativeLibraryPathElementsField = RobustLibraryReflect.getFieldValue(fieldPathList.getClass(), fieldPathList, "nativeLibraryPathElements");
            Object fieldPathListValue = getFieldPathList((Object) dexClassLoader);
            RobustLibraryReflect.setField(fieldPathList.getClass(), fieldPathList, "nativeLibraryPathElements", mergeArrary(nativeLibraryPathElementsField, RobustLibraryReflect.getFieldValue(fieldPathListValue.getClass(), fieldPathListValue, "nativeLibraryPathElements")));
        } else {
            //BaseDexClassLoader#pathList(DexPathList)
            ///** list of native library directory elements */
            //private final File[] nativeLibraryDirectories;
            File[] fileArray = (File[]) RobustLibraryReflect.getFieldValueWithTryCatch(fieldPathList.getClass(), fieldPathList, "nativeLibraryDirectories");
            int length = fileArray.length;
            File[] newFileArray = new File[(length + 1)];
            newFileArray[0] = robustNativeLibraryDirectories;
            System.arraycopy(fileArray, 0, newFileArray, 1, length);
            RobustLibraryReflect.setField(fieldPathList.getClass(), fieldPathList, "nativeLibraryDirectories", newFileArray);
        }
        return true;
    }

    private static Object getFieldPathList(Object obj) throws Exception {
        return RobustLibraryReflect.getFieldValue(Class.forName("dalvik.system.BaseDexClassLoader"), obj, "pathList");
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
