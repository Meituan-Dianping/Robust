package com.meituan.robust.patch.resources.diff;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.meituan.robust.patch.resources.diff.data.APKDiffData;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;

/**
 * Created by hedingxu on 16/11/11.
 */

public class ApkDiffDataReaderAndWriter {
    private final static Type TYPE = new TypeToken<APKDiffData>() {
    }.getType();

    private static Gson gson = new Gson();

    public static APKDiffData readDiffData(File file) {
        if (null == file || !file.exists()) {
            return null;
        }

        BufferedReader br = null;
        String content = null;
        try {
            StringBuilder sb = new StringBuilder();
            // 建立对象fileReader
            FileReader fileReader = new FileReader(file);
            br = new BufferedReader(fileReader);
            String s = null;
            while ((s = br.readLine()) != null) {
                sb.append(s).append('\n');
            }
            // 将字符列表转换成字符串
            content = sb.toString();
        } catch (Exception e) {
        } finally {
            try {
                if (br != null) {
                    br.close();
                }
            } catch (IOException e) {
                // ignored
            }
        }

        String json = content;
        if (null == json || json.length() == 0) {
            return gson.fromJson(json, TYPE);
        }
        return null;
    }

    public static boolean writeDiffData(File file, APKDiffData apkDiffData) {

        String apkDiffDataJson = null;
        try {
            apkDiffDataJson = gson.toJson(apkDiffData, TYPE);
        } catch (Throwable throwable) {
        }

        String content = apkDiffDataJson;
        if (null == file || null == content) {
            return false;
        }
        FileWriter fileWriter = null;
        try {
            fileWriter = new FileWriter(file);
            fileWriter.write(content);
        } catch (IOException e) {
            return false;
        } finally {
            if (fileWriter != null) {
                try {
                    fileWriter.close();
                } catch (IOException e) {
                }
            }
        }
        return true;
    }
}
