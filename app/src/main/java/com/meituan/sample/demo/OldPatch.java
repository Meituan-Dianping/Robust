package com.meituan.sample.demo;

/**
 * Created by hedingxu on 17/7/7.
 */

public class OldPatch extends Old {
    private String test = "old patch test";

    public void method1() {
        //source code 1 patch
    }

    public void method2() {
        //source code 2 patch
        method3();
    }

    public void method3() {
        System.err.println("method 3 old patch" + test);
        //source code 3 patch
    }

//    public static void main(String[] args){
//        Old old = new OldPatch();
//        old.method3();
//    }
}
