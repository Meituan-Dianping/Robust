package com.meituan.robust;

/**
 * Created by zhangmeng on 2017/5/9.
 */

public class RobustArguments {
    public Object[] paramsArray;
    public Object current;
    public  boolean isStatic;
    public int methodNumber;
    public Class[] paramsClassTypes;
    public Class returnType;

    public RobustArguments(Object[] paramsArray, Object current,  boolean isStatic, int methodNumber, Class[] paramsClassTypes, Class returnType) {
        this.paramsArray = paramsArray;
        this.current = current;
        this.isStatic = isStatic;
        this.methodNumber = methodNumber;
        this.paramsClassTypes = paramsClassTypes;
        this.returnType = returnType;
    }
}
