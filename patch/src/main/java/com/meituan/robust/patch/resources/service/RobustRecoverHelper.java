package com.meituan.robust.patch.resources.service;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Process;

/**
 * Created by hedingxu on 17/6/8.
 */

public class RobustRecoverHelper {
    private static RobustRecoverHelper instance;
    private final HandlerThread mTaskThread;
    private final Handler handler;

    synchronized static RobustRecoverHelper getInstance() {
        if (instance == null) {
            instance = new RobustRecoverHelper();
        }
        return instance;
    }

    private RobustRecoverHelper() {
        mTaskThread = new HandlerThread(RobustRecoverHelper.class.getSimpleName(), Process.THREAD_PRIORITY_DEFAULT + Process.THREAD_PRIORITY_MORE_FAVORABLE);
        mTaskThread.start();
        handler = new Handler(mTaskThread.getLooper());
    }

    public void postRunnable(Runnable runnable) {
        handler.post(runnable);
    }
}
