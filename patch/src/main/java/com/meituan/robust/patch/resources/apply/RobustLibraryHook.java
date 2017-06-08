package com.meituan.robust.patch.resources.apply;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Created by hedingxu on 17/6/7.
 */

public class RobustLibraryHook {
    public static Object getFieldValue(Class cls, Object obj, String name) throws Exception {
        Field declaredField = cls.getDeclaredField(name);
        declaredField.setAccessible(true);
        return declaredField.get(obj);
    }

    public static Object getFieldValueWithTryCatch(Class cls, Object obj, String name) {
        try {
            return RobustLibraryHook.getFieldValue(cls, obj, name);
        } catch (Exception e) {
            return null;
        }
    }

    public static void setField(Class cls, Object obj, String fieldName, Object value) throws Exception {
        Field declaredField = cls.getDeclaredField(fieldName);
        declaredField.setAccessible(true);
        declaredField.set(obj, value);
    }

    public static void setFieldWithTryCatch(Class cls, Object obj, String fieldName, Object value) {
        try {
            RobustLibraryHook.setField(cls, obj, fieldName, value);
        } catch (Exception e) {
        }
    }

    public static Object invokeMethodWithNewClass(Class cls, Object obj, String str, Object... objArr) throws Exception {
        Class[] clsArr = null;
        if (objArr != null) {
            Class[] clsArr2 = new Class[objArr.length];
            for (int i = 0; i < objArr.length; i++) {
                clsArr2[i] = objArr[i].getClass();
            }
            clsArr = clsArr2;
        }
        Method declaredMethod = cls.getDeclaredMethod(str, clsArr);
        declaredMethod.setAccessible(true);
        return declaredMethod.invoke(obj, objArr);
    }

    public static Object invokeMethod(Class cls, Object obj, String str, Class[] clsArr, Object... objArr) throws Exception {
        Method declaredMethod = cls.getDeclaredMethod(str, clsArr);
        declaredMethod.setAccessible(true);
        return declaredMethod.invoke(obj, objArr);
    }

    public static Object invokeMethodWithTryCatch(Class cls, Object obj, String str, Class[] clsArr, Object... objArr) {
        try {
            return RobustLibraryHook.invokeMethod(cls, obj, str, clsArr, objArr);
        } catch (Exception e) {
            return null;
        }
    }

    public static Object newInstance(Class cls, Class[] clsArr, Object... objArr) throws Exception {
        Constructor declaredConstructor = cls.getDeclaredConstructor(clsArr);
        declaredConstructor.setAccessible(true);
        return declaredConstructor.newInstance(objArr);
    }
}
