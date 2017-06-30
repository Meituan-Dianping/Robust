package com.meituan.robust;

import com.meituan.robust.common.MD5;

/**
 * Created by hedingxu on 17/6/27.
 */

public class RobustMethodId {
    private RobustMethodId() {

    }

    public static String getMethodId(String methodInfo) {
        return MD5.getHashString(methodInfo);
    }

    public static void main(String[] args) {
//        String key = "com.meituan.android.tower.common.view.CenteredImageSpan.getCachedDrawable()";
        String key = "com.dianping.util.WrapInputStream.available()";
        String value = getMethodId(key);
        System.err.println("getMethodId : " + value);
    }
}
