package com.meituan.robust;

/**
 * Created by zhangmeng on 2017/4/21.
 */

public interface RobustExtension  {
    /**
     *
     * @return 关于接入方信息的描述，请保证各个业务方独立不同，且唯一不变。
     * 建议使用各个业务方包名+一些功能性描述，比如：com.meituan.robust android热更新系统
     */
    String describeSelfFunction();

    /**
     * 通知监听者信息
     * @param msg，msg是describeSelfFunction中的返回内容
     */
    void notifyListner(String msg);

    Object accessDispatch(Object[] paramsArray,Object current,int methodNumber,Class[]paramsTypeArray,Class returnType);

    boolean isSupport(Object[] paramsArray,Object current,int methodNumber,Class[]paramsTypeArray,Class returnType);

}
