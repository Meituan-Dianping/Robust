package com.meituan.robust;

import android.text.TextUtils;

import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by c_kunwu on 16/7/5.
 */
public class PatchProxy {

    private static CopyOnWriteArrayList<RobustExtension> registerExtensionList = new CopyOnWriteArrayList<>();
    private static ThreadLocal<RobustExtension> robustExtensionThreadLocal = new ThreadLocal<>();

    /**
     * 原来的插桩逻辑为这样：
     * <pre>
     * if (PatchProxy.isSupport()) {
     *     return PatchProxy.accessDispatch();
     * }
     * <pre/>
     * 封装一下变成下面这种代码
     * <pre>
     * PatchProxyResult patchProxyResult = PatchProxy.proxy();
     * if (patchProxyResult.isSupported) {
     *     return patchProxyResult.result;
     * }
     * <pre/>
     * 这样做的好处有两个：
     * 1. 减少包大小。 不是开玩笑，虽然后者代码看起来变得复杂，但实质产生的指令更少。
     * 之前两个函数调用，每次都需要load 7个参数到栈上，这7个参数还不是简单的基本类型，这意味着比后者多出若干条指令。
     * 数据显示在5W个方法的插桩下，后者能比前者节省200KB
     *
     * 2. fix一个bug。robust其实支持采用将ChangeQuickRedirect置为null的方法实时下线一个patch，那原来的插桩逻辑就存在线程安全的问题。
     * 根源在于原来的逻辑中ChangeQuickRedirect是每次都直接去取的static变量值
     * 如果在执行isSupport的时候ChangeQuickRedirect有值，但执行到accessDispatch时ChangeQuickRedirect被置为空，那就意味着被patch的方法该次将不执行任何代码
     * 这样会带来一系列的不可知问题。
     * 封装之后能保证这两个方法读取到的ChangeQuickRedirect是同一份。
     */
    public static PatchProxyResult proxy(Object[] paramsArray, Object current, ChangeQuickRedirect changeQuickRedirect, boolean isStatic, int methodNumber, Class[] paramsClassTypes, Class returnType) {
        PatchProxyResult patchProxyResult = new PatchProxyResult();
        if (PatchProxy.isSupport(paramsArray, current, changeQuickRedirect, isStatic, methodNumber, paramsClassTypes, returnType)) {
            patchProxyResult.isSupported = true;
            patchProxyResult.result = PatchProxy.accessDispatch(paramsArray, current, changeQuickRedirect, isStatic, methodNumber, paramsClassTypes, returnType);
        }
        return patchProxyResult;
    }

    public static boolean isSupport(Object[] paramsArray, Object current, ChangeQuickRedirect changeQuickRedirect, boolean isStatic, int methodNumber, Class[] paramsClassTypes, Class returnType) {
        //Robust补丁优先执行，其他功能靠后
        if (changeQuickRedirect == null) {
            //不执行补丁，轮询其他监听者
            if (registerExtensionList == null || registerExtensionList.isEmpty()) {
                return false;
            }
            for (RobustExtension robustExtension : registerExtensionList) {
                if (robustExtension.isSupport(new RobustArguments(paramsArray, current, isStatic, methodNumber, paramsClassTypes, returnType))) {
                    robustExtensionThreadLocal.set(robustExtension);
                    return true;
                }
            }
            return false;
        }
        String classMethod = getClassMethod(isStatic, methodNumber);
        if (TextUtils.isEmpty(classMethod)) {
            return false;
        }
        Object[] objects = getObjects(paramsArray, current, isStatic);
        try {
            return changeQuickRedirect.isSupport(classMethod, objects);
        } catch (Throwable t) {
            return false;
        }
    }


    public static Object accessDispatch(Object[] paramsArray, Object current, ChangeQuickRedirect changeQuickRedirect, boolean isStatic, int methodNumber, Class[] paramsClassTypes, Class returnType) {

        if (changeQuickRedirect == null) {
            RobustExtension robustExtension = robustExtensionThreadLocal.get();
            robustExtensionThreadLocal.remove();
            if (robustExtension != null) {
                notify(robustExtension.describeSelfFunction());
                return robustExtension.accessDispatch(new RobustArguments(paramsArray, current, isStatic, methodNumber, paramsClassTypes, returnType));
            }
            return null;
        }
        String classMethod = getClassMethod(isStatic, methodNumber);
        if (TextUtils.isEmpty(classMethod)) {
            return null;
        }
        notify(Constants.PATCH_EXECUTE);
        Object[] objects = getObjects(paramsArray, current, isStatic);
        return changeQuickRedirect.accessDispatch(classMethod, objects);
    }

    public static void accessDispatchVoid(Object[] paramsArray, Object current, ChangeQuickRedirect changeQuickRedirect, boolean isStatic, int methodNumber, Class[] paramsClassTypes, Class returnType) {
        if (changeQuickRedirect == null) {
            RobustExtension robustExtension = robustExtensionThreadLocal.get();
            robustExtensionThreadLocal.remove();
            if (robustExtension != null) {
                notify(robustExtension.describeSelfFunction());
                robustExtension.accessDispatch(new RobustArguments(paramsArray, current, isStatic, methodNumber, paramsClassTypes, returnType));
            }
            return;
        }
        notify(Constants.PATCH_EXECUTE);
        String classMethod = getClassMethod(isStatic, methodNumber);
        if (TextUtils.isEmpty(classMethod)) {
            return;
        }
        Object[] objects = getObjects(paramsArray, current, isStatic);
        changeQuickRedirect.accessDispatch(classMethod, objects);
    }


    private static Object[] getObjects(Object[] arrayOfObject, Object current, boolean isStatic) {
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

    private static String getClassMethod(boolean isStatic, int methodNumber) {
        String classMethod = "";
        try {
            //可能过于耗时，这部分需要请自己调用函数
//            java.lang.StackTraceElement stackTraceElement = (new java.lang.Throwable()).getStackTrace()[2];
//            String methodName = stackTraceElement.getMethodName();
//            String className = stackTraceElement.getClassName();
            String methodName = "";
            String className = "";
            classMethod = className + ":" + methodName + ":" + isStatic + ":" + methodNumber;
        } catch (Exception e) {

        }
        return classMethod;
    }

    private static String[] getClassMethodName() {
        java.lang.StackTraceElement stackTraceElement = (new java.lang.Throwable()).getStackTrace()[2];
        String[] classMethodname = new String[2];
        classMethodname[0] = stackTraceElement.getClassName();
        classMethodname[1] = stackTraceElement.getMethodName();
        return classMethodname;
    }

    /***
     *
     * @param robustExtension
     * 注册RobustExtension监听器，通知当前执行程序
     * @return
     */
    public synchronized static boolean register(RobustExtension robustExtension) {
        if (registerExtensionList == null) {
            registerExtensionList = new CopyOnWriteArrayList<RobustExtension>();
        }
        return registerExtensionList.addIfAbsent(robustExtension);
    }

    public synchronized static boolean unregister(RobustExtension robustExtension) {
        if (registerExtensionList == null) {
            return false;
        }
        return registerExtensionList.remove(robustExtension);
    }

    /**
     * clear registerExtensionList and executing robustExtension
     */
    public static void reset() {
        registerExtensionList = new CopyOnWriteArrayList<>();
        robustExtensionThreadLocal = new ThreadLocal<>();
    }

    private static void notify(String info) {
        if (registerExtensionList == null) {
            return;
        }
        for (RobustExtension robustExtension : registerExtensionList) {
            robustExtension.notifyListner(info);
        }
    }

}
