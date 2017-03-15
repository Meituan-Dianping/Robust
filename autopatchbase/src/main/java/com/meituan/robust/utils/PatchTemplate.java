package com.meituan.robust.utils;


import com.meituan.robust.ChangeQuickRedirect;

import java.util.Map;
import java.util.WeakHashMap;

/**
 * Created by mivanzhang on 16/7/26.
 */
public class PatchTemplate implements ChangeQuickRedirect {
    public static final String MATCH_ALL_PARAMETER = "(\\w*\\.)*\\w*";

    public PatchTemplate() {
    }

    private static final Map<Object, Object> keyToValueRelation = new WeakHashMap<>();

    @Override
    public Object accessDispatch(String methodName, Object[] paramArrayOfObject) {
        return null;
    }

    @Override
    public boolean isSupport(String methodName, Object[] paramArrayOfObject) {
        return true;
    }
}
