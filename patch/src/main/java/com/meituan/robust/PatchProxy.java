package com.meituan.robust;

import android.text.TextUtils;

/**
 * Created by c_kunwu on 16/7/5.
 */
public class PatchProxy {
    static public boolean isSupport(Object[] arrayOfObject, Object current, ChangeQuickRedirect changeQuickRedirect, boolean isStatic, int methodNumber) {
        if (changeQuickRedirect == null) {
            return false;
        }
        String classMethod = getClassMethod(isStatic, methodNumber);
        if (TextUtils.isEmpty(classMethod)) {
            return false;
        }
        Object[] objects = getObjects(arrayOfObject, current, isStatic);
        try {
            return changeQuickRedirect.isSupport(classMethod, objects);
        } catch (Throwable t) {
            return false;
        }
    }

    static public Object accessDispatch(Object[] arrayOfObject, Object current, ChangeQuickRedirect changeQuickRedirect, boolean isStatic, int methodNumber) {
        if (changeQuickRedirect == null) {
            return null;
        }
        String classMethod = getClassMethod(isStatic, methodNumber);
        if (TextUtils.isEmpty(classMethod)) {
            return null;
        }
        Object[] objects = getObjects(arrayOfObject, current, isStatic);
        return changeQuickRedirect.accessDispatch(classMethod, objects);
    }

    static public void accessDispatchVoid(Object[] arrayOfObject, Object current, ChangeQuickRedirect changeQuickRedirect, boolean isStatic, int methodNumber) {
        if (changeQuickRedirect == null) {
            return;
        }
        String classMethod = getClassMethod(isStatic, methodNumber);
        if (TextUtils.isEmpty(classMethod)) {
            return;
        }
        Object[] objects = getObjects(arrayOfObject, current, isStatic);
        changeQuickRedirect.accessDispatch(classMethod, objects);
    }


    static private Object[] getObjects(Object[] arrayOfObject, Object current, boolean isStatic) {
        Object[] objects;
        if (arrayOfObject == null) {
            return null;
        }
        int argNum = arrayOfObject.length;
        if (isStatic) {
            objects = new Object[argNum];
        } else {
            objects = new Object[argNum + 1];
        }
        int x = 0;
        for (; x < argNum; x++) {
            objects[x] = arrayOfObject[x];
        }
        if (!(isStatic)) {
            objects[x] = current;
        }
        return objects;
    }

    static private String getClassMethod(boolean isStatic, int methodNumber) {
        String classMethod = "";
        try {
            java.lang.StackTraceElement stackTraceElement = (new java.lang.Throwable()).getStackTrace()[2];
            String methodName = stackTraceElement.getMethodName();
            String className = stackTraceElement.getClassName();
            classMethod = className + ":" + methodName + ":" + isStatic + ":" + methodNumber;
        } catch (Throwable t) {
        }
        return classMethod;
    }

}
