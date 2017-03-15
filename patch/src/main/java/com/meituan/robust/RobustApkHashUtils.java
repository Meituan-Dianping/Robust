package com.meituan.robust;

import android.content.Context;
import android.text.TextUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by hedex on 17/2/27.
 */
public class RobustApkHashUtils {
    private static String robustApkHashValue;

    public static String readRobustApkHash(Context context) {

        if (TextUtils.isEmpty(robustApkHashValue)) {
            robustApkHashValue = readRobustApkHashFile(context);
        }

        return robustApkHashValue;
    }

    private static String readRobustApkHashFile(Context context) {
        String value = "";
        if (null == context) {
            return value;
        }

        try {
            value = readFirstLine(context, Constants.ROBUST_APK_HASH_FILE_NAME);
        } catch (Throwable throwable) {

        }

        return value;
    }

    private static String readFirstLine(Context context, String fileName) {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(
                    new InputStreamReader(context.getAssets().open(fileName)));

            return reader.readLine();
        } catch (IOException e) {
            return "";
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                }
            }
        }
    }

}
