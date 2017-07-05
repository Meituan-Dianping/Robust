package com.meituan.robust.patch.resources.apply;

import android.content.Context;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Created by hedingxu on 17/6/6.
 */

public class RobustResourceReflect {
    private RobustResourceReflect(){

    }
    private static Object sThreadLocal;

    //@UiThread
    public static Object getCurrentActivityThread(Context context) {
        if (null == context) {
            return null;
        }

        if (sThreadLocal == null) {
            sThreadLocal = getCurrentActivityThread1(context);
        }
        if (sThreadLocal == null) {
            sThreadLocal = getCurrentActivityThread2(context);
        }
        return sThreadLocal;
    }

    //@UiThread
    private static Object getCurrentActivityThread1(Context context) {
        Object obj = null;

        try {
            Class cls = Class.forName("android.app.ActivityThread");
            try {
                obj = getFieldValue(cls, null, "sCurrentActivityThread");
            } catch (Exception e) {
            }
            if (obj == null) {
                obj = ((ThreadLocal) getFieldValue(cls, null, "sThreadLocal")).get();
            }
            return obj;
        } catch (Exception e2) {
            e2.printStackTrace();
        }
        return null;

    }

    //@UiThread
    private static Object getCurrentActivityThread2(Context context) {
        try {
            Class activityThread = Class.forName("android.app.ActivityThread");
            Method m = activityThread.getMethod("currentActivityThread");
            m.setAccessible(true);
            Object currentActivityThread = m.invoke(null);
            if (currentActivityThread == null && context != null) {
                // In older versions of Android (prior to frameworks/base 66a017b63461a22842)
                // the currentActivityThread was built on thread locals, so we'll need to try
                // even harder
                Field mLoadedApk = context.getClass().getField("mLoadedApk");
                mLoadedApk.setAccessible(true);
                Object apk = mLoadedApk.get(context);
                Field mActivityThreadField = apk.getClass().getDeclaredField("mActivityThread");
                mActivityThreadField.setAccessible(true);
                currentActivityThread = mActivityThreadField.get(apk);
            }
            return currentActivityThread;
        } catch (Throwable ignore) {
            return null;
        }
    }

    private static Object getFieldValue(Class cls, Object obj, String name) throws Exception {
        Field declaredField = cls.getDeclaredField(name);
        declaredField.setAccessible(true);
        return declaredField.get(obj);
    }

}
