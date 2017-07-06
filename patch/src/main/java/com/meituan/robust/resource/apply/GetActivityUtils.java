package com.meituan.robust.resource.apply;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.util.ArrayMap;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * modified by hedex
 * Apply a change to the current activity.
 */
class GetActivityUtils {
    private GetActivityUtils() {

    }

    public static Activity getForegroundActivity(/*@Nullable */ Context context) {
        List<Activity> list = getActivities(context, true);
        return list.isEmpty() ? null : list.get(0);
    }

    public static List<Activity> getAllCurrentActivities(Context context){
        return getActivities(context, false);
    }
    // http://stackoverflow.com/questions/11411395/how-to-get-current-foreground-activity-context-in-android
    public static List<Activity> getActivities(/*@Nullable */ Context context, boolean foregroundOnly) {
        List<Activity> list = new ArrayList<Activity>();
        try {
            Class activityThreadClass = Class.forName("android.app.ActivityThread");
            Object activityThread = RobustResourceReflect.getCurrentActivityThread(context);
            Field activitiesField = activityThreadClass.getDeclaredField("mActivities");
            activitiesField.setAccessible(true);
            Collection c;
            Object collection = activitiesField.get(activityThread);
            if (collection instanceof HashMap) {
                // Older platforms
                Map activities = (HashMap) collection;
                c = activities.values();
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT &&
                    collection instanceof ArrayMap) {
                ArrayMap activities = (ArrayMap) collection;
                c = activities.values();
            } else {
                return list;
            }
            for (Object activityRecord : c) {
                Class activityRecordClass = activityRecord.getClass();
                if (foregroundOnly) {
                    Field pausedField = activityRecordClass.getDeclaredField("paused");
                    pausedField.setAccessible(true);
                    if (pausedField.getBoolean(activityRecord)) {
                        continue;
                    }
                }
                Field activityField = activityRecordClass.getDeclaredField("activity");
                activityField.setAccessible(true);
                Activity activity = (Activity) activityField.get(activityRecord);
                if (activity != null) {
                    list.add(activity);
                }
            }
        } catch (Throwable ignore) {
        }
        return list;
    }

}