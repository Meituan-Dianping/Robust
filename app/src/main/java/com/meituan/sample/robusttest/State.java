package com.meituan.sample.robusttest;

import android.util.Log;

import com.meituan.sample.robusttest.other.Hll;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by c_kunwu on 16/5/5.
 */
public class State<T> implements Cloneable, Comparable {
    public static int index = 0;
    public int index1 = 0;
    public String tag = "current";
    public Hll hll;

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

    @Override
    public int compareTo(Object another) {
        return 0;
    }

    public int getTime() {
        return 0;
    }

    public String getName() {
        return null;
    }

    public void setName(String name) {


    }
}

