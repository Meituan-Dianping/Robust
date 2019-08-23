package com.meituan.robust;

/**
 * @author feelschaotic
 * @create 2019/7/30.
 */

public interface RollbackListener {
    void onRollback(String methodsId, String methodLongName, Throwable e);

    boolean getRollback(String methodsId);
}
