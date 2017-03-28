package com.meituan.sample.robusttest;

import com.meituan.robust.patch.annotaion.Modify;

/**
 * Created by mivanzhang on 17/2/20.
 */

public class SampleClass {
    protected String flag = "flagstring";
    protected static String name = "zhang";
    public int times = 6;

    public String getName() {
        return name;
    }
    @Modify
    public  int multiple(int number) {
        Children pair=new Children();
        pair.setFirst("asdad");
        number= changeInputs(number);
        System.out.print("hellow world 1");
        return times*number;
    }

    public  int changeInputs(int number) {

        return number;
    }


    public class Parent {
        private String first=null;
        //混淆为a
        private void privateMetthod(){{
            System.out.println("Robust");
        }}
        //混淆为b
        public void setFirst(String fir){{
            first=fir;
            Parent children=new Children();
            children.privateMetthod();
        }}
    }
    public class Children extends Parent{
        private String sencod=null;
        //混淆为a
        public void setSecond(String fir){
            this.sencod=fir;
        }
    }

}

