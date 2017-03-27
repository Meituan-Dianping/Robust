package com.meituan.sample.robusttest;

import android.util.Log;

import com.meituan.sample.robusttest.other.Hll;
import com.meituan.robust.patch.annotaion.Modify;
import com.meituan.sample.SecondActivity;

/**
 * Created by mivanzhang on 16/7/21.
 */

public class Super extends Hll {
    protected String flag = "flagstring";
    protected static String name = "zhang";
    public int times = 0;
    public byte byteFiled = 2;
    private static String staticStringField = "meituan";
    static int staticIntField = 12311111;
    private long longField = 123l;
    private Hll hll = new Hll(true);
    private static State state = new State(new Hll(true));
    private String hllString = "SuperString";

    static {
        staticStringField = "I am static String field ";
        name = "I am static String name ";
    }



    public String[] methodWithArrayParameters(String[] flag) {
        return flag;
    }

    @Modify
    public String getText1(int i, Long j, Integer x) {
        hll.callBack();
        CallBack callBack = hll;
        callBack.callBack();
        Super s = new Super();
        times = 1 / 0;
        Log.d("robust", "test static block  ");
        Log.d("robust", staticStringField);
        Log.d("robust", name);
        SecondActivity.methodWithArrayParameters(new String[]{"ad", "bc"});
        Log.d("robust", "in Super.getText1()  ");

        s.flag = "new s.flag";
        s.getText();
        flag = s.flag;
        times = 1123;
        s.times = times;
        Log.d("robust", "state.get()   " + state.get());
        state.index1 = times;
        return "";
//        return flag + "   " + getinstance(s) + "   " + times + "   " + staticIntField;
    }

    private Super xxx(Super s) {
        return s;
    }

    public Object getinstance() {
        privateMethod(1, "asd");
        Super s = new Super();
        s.times = 1;
        return s;
    }
    private String privateMethod(int index, String name) {
        hll.callBack();

        return "privateMethod";
    }
    public String getText() {
        privateMethod(1,"name");
        Log.d("robust", "in Super.getText()  ");
        Inner inner = new Inner(1, "meng");
        inner.setName("mivanzhang");
        inner.getAnswer("mivanzhang");
//        return "hello world";
        return "  you make it!!   " + inner.getAnswer("meituan");
    }

    static String staticMethod() {
        return "www.meituan.com " + staticStringField;
    }

    static class StaticInstance {
        static Super instance = new Super();
    }

    class Inner {
        private int time;
        private String name;
        private Hll hll = new Hll(true);

        public int getTime() {
            return time;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            Log.d("robust", "in Super.Inner.setName()  ");
            this.name = name;
        }

        public String getAnswer(String name) {
            Log.d("robust", "in Super.Inner.getAnswer()  ");
            hll.getStrings(times, name);
            setName(name);
            return "   success !!!!you make it  " + name;
//            return "hello world ,I am Robust!!" + name;
        }

        public Inner(int time, String name, Hll hll) {
            Log.d("robust", "in Super.Inner.Inner()  ");
            this.time = time;
            this.name = name;
            this.hll = hll;
        }

        public Inner(int time, String name) {
            this(time, name, new Hll(false));
        }
    }


    public void test(NoField n) {
        n.hashCode();
    }
}