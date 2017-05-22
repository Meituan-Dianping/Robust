package com.meituan.sample;

import android.util.Log;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;

/**
 * Created by dongxiabin on 16/6/23.
 */
@Aspect
public class DotClassExceptionAspect {

    static final String TAG = "DemoAspect";

    @Pointcut("execution(* com.meituan.sample.SecondActivity.onCreate(..))")
    public void afterLog() throws Throwable {
    }

    @After("afterLog()")
    public void afterInserLog(JoinPoint joinPoint) throws Throwable {
        Log.e(TAG, " After insert log by DotClassExceptionAspect");
    }


    @Pointcut("execution(* com.meituan.sample.SecondActivity.onCreate(..))")
    public void realActivityStartPc() {
    }

    @Before("realActivityStartPc()")
    public void logStartAd(JoinPoint joinPoint) {
        Log.e(TAG, "before insert by DotClassExceptionAspect");
    }

    //=================================================================
    //=================================================================
    @Around("execution(* com.meituan.sample.MainActivity.onCreate(..))")
    public void startActivity(ProceedingJoinPoint point) throws Throwable {
        catchActivityNotFoundException(point);
    }

    private void catchActivityNotFoundException(ProceedingJoinPoint point) throws Throwable {
        Log.e(TAG, point.toShortString());
        point.proceed(point.getArgs());
    }

}
