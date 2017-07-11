package com.meituan.sample.demo;
public class Old {

    OldPatch oldPatch;

    private String test = "old test";

    public void method1(){
        if (null != oldPatch){
            oldPatch.method1();
            return;
        }
        //source code 1
    }

    public /*final*/ void method2(){
//        if (null != oldPatch){
//            oldPatch.method2();
//            return;
//        }
        //source code 2
        method3();
    }

    private void method3(){
        System.err.println("method 3 old " + test);
//        if (null != oldPatch){
//            oldPatch.method3();
//            return;
//        }
        //source code 3
    }

    public static void main(String[] args){
        Old old = new OldPatch();
        old.method3();
    }

}
