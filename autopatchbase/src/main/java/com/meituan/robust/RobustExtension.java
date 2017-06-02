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
     * 通知监听者信息，通知哪个监听者被执行了
     * @param msg，msg是describeSelfFunction中的返回内容
     */
    void notifyListner(String msg);
    /**
     * @param paramsArray 原方法的参数列表
     * @param current 当前对象的引用，即this对象，如果是static方法，值为null
     * @param methodNumber 方法的唯一编号
     * @param paramsTypeArray 方法参数类型列表
     * @param returnType 方法的返回值类型
     * 在方法的方法体最先执行的代码逻辑，请注意这个方法执行之后，原方法体中的其他逻辑不在执行
     * @return
     *
     */
    Object accessDispatch(RobustArguments robustArguments);

    /**
     *@param paramsArray 原方法的参数列表
     * @param current 当前对象的引用，即this对象，如果是static方法，值为null
     * @param methodNumber 方法的唯一编号
     * @param paramsTypeArray 方法参数类型列表
     * @param returnType 方法的返回值类型
     *
     * @return return true 代表不继续执行原有方法体，只执行accessDispatch方法的逻辑，并把accessDispatch的方法返回值作为原函数的返回值
     *          return false 代表执行原有方法体，但是可以在isSupport方法里面添加额外的逻辑，比如说记录当前的方法调用栈或者日志等
     */
    boolean isSupport(RobustArguments robustArguments);

}
