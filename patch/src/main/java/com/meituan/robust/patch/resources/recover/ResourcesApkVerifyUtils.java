package com.meituan.robust.patch.resources.recover;

import com.meituan.robust.common.FileUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by hedingxu on 17/6/5.
 * 仅用于读写MD5这样简答的内容
 */
public class ResourcesApkVerifyUtils {

    private ResourcesApkVerifyUtils() {

    }

    static boolean writeResourcesApkMd5(File resourcesApkMd5File, String md5) {
        return sampleWriteFile(resourcesApkMd5File, md5);
    }

    static String readResourcesApkMd5(File resourcesApkMd5File) {
        return sampleReadFile(resourcesApkMd5File);
    }

    private static String sampleReadFile(File file) {
        if (null == file || !file.exists()) {
            return "";
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
            }
        }
        return content;
    }

    private static boolean sampleWriteFile(File file, String content) {
        if (null == file) {
            return false;
        }
        FileUtil.createFile(file.getAbsolutePath());
        FileWriter fileWriter = null;
        try {
            fileWriter = new FileWriter(file);
            fileWriter.write(content);
            return true;
        } catch (IOException e) {
        } finally {
            if (fileWriter != null) {
                try {
                    fileWriter.close();
                } catch (IOException e) {
                }
            }
        }

        return false;
    }
}
