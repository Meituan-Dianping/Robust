package com.meituan.sample.extension;

import android.util.Log;

import com.meituan.robust.RobustArguments;
import com.meituan.robust.RobustExtension;

/**
 * Created by zhangmeng on 2017/5/9.
 */

public class LogExtension implements RobustExtension {
    @Override
    public String describeSelfFunction() {
        return "LogExtension Example";
    }

    @Override
    public void notifyListner(String msg) {
        //you can know here which RobustExtension execute by msg
    }

    @Override
    public Object accessDispatch(RobustArguments robustArguments) {
        return null;
    }

    @Override
    public boolean isSupport(RobustArguments robustArguments) {
        //因为我们只是在方法体前插入了一行log，没有必要替换原方法的逻辑，所以这个函数的返回值是false,
        //如果这个函数的返回值是true,那么原方法体的方法逻辑不在被执行，会执行accessDispatch方法逻辑。
        Log.d("LogExtension","arrived here class is "+this.toString());
        return false;
    }
}
