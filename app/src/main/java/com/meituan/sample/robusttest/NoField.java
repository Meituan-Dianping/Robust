package com.meituan.sample.robusttest;

import android.util.Log;

/**
 * Created by mivanzhang on 16/10/21.
 */
public class NoField {
    @Override
    public int hashCode() {
        Log.d("robusttest", "in hashCode()");
        return super.hashCode();
    }

    @Override
    public String toString() {

        getName();
        Log.d("robusttest", "after getName()");
        return super.toString();
    }

    private NoField getName() {
        this.hashCode();
        JustTest s = new JustTest();
        s.test(this);
        Log.d("robusttest", "after hashCode()");
        return StaticInner.instance;
    }

    static class StaticInner {
        static NoField instance = new NoField();
    }
}
