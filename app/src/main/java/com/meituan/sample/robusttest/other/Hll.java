package com.meituan.sample.robusttest.other;


import android.util.Log;

import com.meituan.sample.robusttest.CallBack;

import static android.R.attr.name;
import static com.meituan.sample.robusttest.State.index;

/**
 * Created by c_kunwu on 16/5/13.
 */

public class Hll implements CallBack {
    private boolean needToP = false;
    private String hllString = "hllString";

    public Hll(boolean t) {
        this.needToP = t;
    }

    public Hll() {

    }
    private String privateMethod(int index, String name) {
        Log.d("robust", "in hll.getStrings()  ");
        packageMethod(1,name);
        return index + " needToP not equal true  " + name;
    }
    public int needP() {
        privateMethod(1,"asd");
        super.toString();
        getStrings(1,"meituan");
        return needToP ? 1 : 0;
    }

    public String getStrings(int index, String name) {
        Log.d("robust", "in hll.getStrings()  ");
        packageMethod(1,name);
        return index + " needToP not equal true  " + name;
    }



    String packageMethod(int index, String name) {
        Log.d("robust", "in hll.packageMethod()  ");
        super.toString();
        if (needToP) {
            return index + " needToP ==true  " + name;
        }
        return index + " needToP not equal true  " + name;
    }

    public String protextedMethod() {
        if (needToP) {
            hllString=index + " needToP ==true  " + name;
        }
        return hllString;
    }

    public int check() {
        hllString="hllString checked";
        if (needToP) {
            hllString=index + " needToP ==true  " + name;
        }
        return 0;
    }

    @Override
    public void callBack() {

    }
}
