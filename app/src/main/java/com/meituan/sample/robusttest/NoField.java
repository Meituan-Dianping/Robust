package com.meituan.sample.robusttest;

/**
 * Created by mivanzhang on 16/10/21.
 */
public class NoField {
    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public String toString() {

        return super.toString();
    }

    private NoField getName() {
        this.hashCode();
        JustTest s = new JustTest();
        s.test(this);
        return StaticInner.instance;
    }

    static class StaticInner {
        static NoField instance = new NoField();
    }
}
