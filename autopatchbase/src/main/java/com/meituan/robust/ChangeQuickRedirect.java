package com.meituan.robust;

/**
 * Created by c_kunwu on 16/5/10.
 */
public interface ChangeQuickRedirect {
    Object accessDispatch(String methodName, Object[] paramArrayOfObject);

    boolean isSupport(String methodName, Object[] paramArrayOfObject);
}
