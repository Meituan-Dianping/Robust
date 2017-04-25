package com.meituan.robust;

/**
 * Created by zhangmeng on 2017/4/25.
 */

public class Arguments {
    public Object[] paramsArray;
    public Object current;
    public ChangeQuickRedirect changeQuickRedirect;
    public boolean isStatic;
    public int methodNumber;
    public Class[] paramsClassTypes;
    public Class returnType;

    public Arguments(Object[] paramsArray, Object current, ChangeQuickRedirect changeQuickRedirect, boolean isStatic, int methodNumber, Class[] paramsClassTypes, Class returnType) {
        this.paramsArray = paramsArray;
        this.current = current;
        this.changeQuickRedirect = changeQuickRedirect;
        this.isStatic = isStatic;
        this.methodNumber = methodNumber;
        this.paramsClassTypes = paramsClassTypes;
        this.returnType = returnType;
    }
}
