//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.meituan.sample;

import org.aspectj.runtime.internal.AroundClosure;

class MainActivity2 extends AroundClosure {
//    public static ChangeQuickRedirect changeQuickRedirect;
    public MainActivity2(Object[] var1) {
        super(var1);
    }

    public Object run(Object[] var1) {
//        if(changeQuickRedirect != null) {
//            if(PatchProxy.isSupport(var1, this, changeQuickRedirect, false, 1,new Class[]{Object[].class},Object.class)) {
//                return PatchProxy.accessDispatch(var1, this, changeQuickRedirect, false, 1,new Class[]{Object[].class},Object.class);
//            }
//        }
//        return MainActivity2.changeQuickRedirect != null && PatchProxy.isSupport(var1, this, MainActivity2.changeQuickRedirect, false, 1, new Class[]{Object[].class}, Object.class)?PatchProxy.accessDispatch(var1, this, MainActivity2.changeQuickRedirect, false, 1, new Class[]{Object[].class}, Object.class):null;
        System.out.println("in MainActivity onCreate ");
        int x=Integer.valueOf("123");
        return x;
    }

//    public static ChangeQuickRedirect changeQuickRedirect;

//    public MainActivity2(Object[] var1) {
//        super(var1);
//    }
//    public Object run(Object[] var1) {
//        Object x = null;
//        if(PatchProxy.isSupport(new Object[]{var1}, this, changeQuickRedirect, false, 11, new Class[]{Object[].class}, Object.class)) {
//            return (Object)PatchProxy.accessDispatch(new Object[]{var1}, this, changeQuickRedirect, false, 11, new Class[]{Object[].class}, Object.class);
//        } else {
//            int x1 = Integer.valueOf("123").intValue();
//            return Integer.valueOf(x1);
//        }
//    }
}
