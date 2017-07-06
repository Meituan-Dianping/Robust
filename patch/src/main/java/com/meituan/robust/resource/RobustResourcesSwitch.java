
package com.meituan.robust.resource;

import android.content.Context;
import android.content.SharedPreferences;

public class RobustResourcesSwitch {

    //    利用shredPreferences资源FIX的总开关，如果出现情况，可以关闭这个开关
    private final static String ROBUST_SP = "robust_sp";
    private final static String ROBUST_RESOURCES_SWITCH = "robust_resources_switch";
    // TODO: 17/5/30 默认值是true,上线之前改成false
    private static boolean resourcesSwitch = true;

    public static boolean getResourcesSwitch(Context context) {
        SharedPreferences sp = context.getSharedPreferences(ROBUST_SP, Context.MODE_MULTI_PROCESS);
        resourcesSwitch = sp.getBoolean(ROBUST_RESOURCES_SWITCH, resourcesSwitch);
        return resourcesSwitch;
    }

    public static void setResourcesSwitch(Context context, boolean onOff) {
        SharedPreferences sp = context.getSharedPreferences(ROBUST_SP, Context.MODE_MULTI_PROCESS);
        sp.edit().putBoolean(ROBUST_RESOURCES_SWITCH, onOff).apply();
        resourcesSwitch = onOff;
    }
}
