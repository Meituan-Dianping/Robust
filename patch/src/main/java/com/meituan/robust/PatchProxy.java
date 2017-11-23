package com.meituan.robust;

import android.text.TextUtils;

import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by c_kunwu on 16/7/5.
 */
public class PatchProxy {

    private static CopyOnWriteArrayList<RobustExtension> registerExtensionList=new CopyOnWriteArrayList<>();
    private static ThreadLocal<RobustExtension> robustExtensionThreadLocal =new ThreadLocal<>();


    public static boolean isSupport(Object[] paramsArray, Object current, ChangeQuickRedirect changeQuickRedirect, boolean isStatic, int methodNumber,Class[] paramsClassTypes,Class returnType) {
        //Robust补丁优先执行，其他功能靠后
        if (changeQuickRedirect == null) {
            //不执行补丁，轮询其他监听者
            if(registerExtensionList ==null|| registerExtensionList.isEmpty()){
                return false;
            }
            for(RobustExtension robustExtension: registerExtensionList){
                if(robustExtension.isSupport(new RobustArguments(paramsArray,current,isStatic, methodNumber, paramsClassTypes, returnType))){
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


    public static Object accessDispatch(Object[] paramsArray, Object current, ChangeQuickRedirect changeQuickRedirect, boolean isStatic, int methodNumber,Class[] paramsClassTypes,Class returnType) {

        if (changeQuickRedirect == null) {
            RobustExtension robustExtension = robustExtensionThreadLocal.get();
            robustExtensionThreadLocal.remove();
            if(robustExtension !=null){
                notify(robustExtension.describeSelfFunction());
                return robustExtension.accessDispatch(new RobustArguments(paramsArray,current,isStatic, methodNumber, paramsClassTypes, returnType));
            }
            return null;
        }
        String classMethod = getClassMethod(isStatic, methodNumber);
        if (TextUtils.isEmpty(classMethod)) {
            return null;
        }
        notify(Constants.PATCH_EXECUTE);
        Object[] objects = getObjects(paramsArray,  current,  isStatic);
        return  changeQuickRedirect.accessDispatch(classMethod, objects);
    }

    public static void accessDispatchVoid(Object[] paramsArray, Object current, ChangeQuickRedirect changeQuickRedirect, boolean isStatic, int methodNumber,Class[] paramsClassTypes,Class returnType) {
        if (changeQuickRedirect == null) {
            RobustExtension robustExtension = robustExtensionThreadLocal.get();
            robustExtensionThreadLocal.remove();
            if(robustExtension !=null){
                notify(robustExtension.describeSelfFunction());
                robustExtension.accessDispatch(new RobustArguments(paramsArray,current,isStatic, methodNumber, paramsClassTypes, returnType));
            }
            return;
        }
        notify(Constants.PATCH_EXECUTE);
        String classMethod = getClassMethod(isStatic, methodNumber);
        if (TextUtils.isEmpty(classMethod)) {
            return ;
        }
        Object[] objects = getObjects( paramsArray,  current,  isStatic);
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
            String methodName="";
            String className="";
            classMethod = className + ":" + methodName + ":" + isStatic + ":" + methodNumber;
        }catch (Exception e){

        }
        return classMethod;
    }

   private static String[] getClassMethodName() {
        java.lang.StackTraceElement stackTraceElement = (new java.lang.Throwable()).getStackTrace()[2];
        String[] classMethodname=new String [2];
        classMethodname[0]=stackTraceElement.getClassName();
        classMethodname[1]=stackTraceElement.getMethodName();
        return classMethodname;
    }

    /***
     *
     * @param robustExtension
     * 注册RobustExtension监听器，通知当前执行程序
     * @return
     */
    public synchronized static boolean register(RobustExtension robustExtension){
        if(registerExtensionList ==null){
            registerExtensionList =new CopyOnWriteArrayList<RobustExtension>();
        }
        return registerExtensionList.addIfAbsent(robustExtension);
    }

    public synchronized static boolean unregister(RobustExtension robustExtension){
        if(registerExtensionList ==null){
            return false;
        }
        return registerExtensionList.remove(robustExtension);
    }

    /**
     * clear registerExtensionList and executing robustExtension
     */
    public static void reset(){
        registerExtensionList =new CopyOnWriteArrayList<>();
        robustExtensionThreadLocal =new ThreadLocal<>();
    }

    private static void notify(String info){
        if(registerExtensionList ==null){
            return;
        }
       for(RobustExtension robustExtension: registerExtensionList){
           robustExtension.notifyListner(info);
       }
    }

}
