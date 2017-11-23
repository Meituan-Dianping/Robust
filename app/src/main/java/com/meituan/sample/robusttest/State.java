package com.meituan.sample.robusttest;

import android.util.Log;

import com.meituan.sample.robusttest.other.Hll;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by c_kunwu on 16/5/5.
 */
public class State<T> extends AsbtractInner implements Cloneable, Comparable {
    public static int index = 0;
    public int index1 = 0;
    public byte byteFiled = 0;
    public String tag = "current";
    public Hll hll;

    static {
        index = 100;
    }

    public long getIndex() {
        getText(1, 2L, 1);
        return -100;
    }

    public String getText(int i, Long j, Integer x) {
        State state = new State(new Hll(true));
        state.index1 = 3;
        state.hll.needP();
        Hll h = state.hll;
        state.get();
        h.needP();
        state.setIndex(hll, 1, 2L, new Object());
        return tag + ": ";
    }


    public State(Hll hll) {
        this.hll = hll;
    }

    public void setIndex(Hll hll, int i, Long j, Object x) {
        this.hll = hll;
        hll.needP();
//        State.staticMethod();
    }

    public List<T> getT(Hll hll, int i, Long j, Object x) {
        List<T> list = new ArrayList<>();
        Integer p = Integer.reverse(index);
        list.add((T) p);
        return list;
    }

    public T get() {
        Log.d("robust", "in state.get()  ");
        return getT(hll, 0, 1L, new Object()).get(0);
    }

    String packageMethod(int index, String name) {
        Log.d("robust", "in Sate.packageMethod()  ");
        return index + " needToP not equal true  " + name;
    }

    @Override
    public int compareTo(Object another) {
        return 0;
    }

    @Override
    public int getTime() {
        return 0;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public void setName(String name) {


    }


    class Inner {
        private int time;
        private String name;

        public int getTime() {
            return time;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getAnswer(String name) {
//            return "you make it";
            return "hell world";
        }
    }
}

abstract class AsbtractInner {

    public abstract int getTime();

    public abstract String getName();

    public abstract void setName(String name);

    public String getAnswer(String name) {
//            return "you make it";
        return "hell world";
    }
}
