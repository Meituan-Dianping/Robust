package com.meituan.robust.resource.service;

import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.MessageQueue;
import android.os.Process;
import android.util.Log;

import com.meituan.robust.resource.util.ProcessUtil;

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

    public void postRunnableDelay(Runnable runnable, long delayMillis) {
        handler.postDelayed(runnable, delayMillis);
    }

    public void killRobustProcessWhenEmpty(final Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mTaskThread.getLooper().getQueue().addIdleHandler(new MessageQueue.IdleHandler() {
                @Override
                public boolean queueIdle() {
                    if (ProcessUtil.isRobustProcess(context)){
                        Log.d("robust","robust process is empty");
                        Log.d("robust","kill robust process");
                        ProcessUtil.killSelf();
                    }
                    return false;
                }
            });
        }
    }

}
