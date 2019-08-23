package com.meituan.robust;

public class RollbackManager {

    private RollbackListener listener;


    private static class RollbackManagerHolder {
        private static final RollbackManager INSTANCE = new RollbackManager();
    }

    public static RollbackManager getInstance() {
        return RollbackManagerHolder.INSTANCE;
    }

    private RollbackManager() {
    }

    public void setRollbackListener(RollbackListener listener) {
        this.listener = listener;
    }

    public boolean getRollback(String methodsId) {
        return listener != null && listener.getRollback(methodsId);
    }

    /**
     * 当异常时重置标志位，表明这是一个旧补丁
     *
     * @return
     */
    public void notifyOnException(String methodsId, String methodLongName, Throwable e) {
        if (listener != null) {
            listener.onRollback(methodsId, methodLongName, e);
        }
    }
}