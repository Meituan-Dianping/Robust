package com.meituan.robust.patch.resources.diff;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.meituan.robust.common.FileUtil;
import com.meituan.robust.common.TxtFileReaderAndWriter;
import com.meituan.robust.patch.resources.diff.data.APKDiffData;
import com.meituan.robust.patch.resources.diff.data.DataUnit;

import java.io.File;
import java.lang.reflect.Type;

/**
 * Created by hedingxu on 16/11/11.
 */

public class ApkDiffDataReaderAndWriter {
    private final static Type TYPE = new TypeToken<APKDiffData>() {
    }.getType();

    private static Gson gson = new GsonBuilder().enableComplexMapKeySerialization().setPrettyPrinting().create();

    public static APKDiffData readDiffData(File file) {
        if (null == file || !file.exists()) {
            return null;
        }

        String json = TxtFileReaderAndWriter.readFileAsString(file);
        if (null == json || json.length() == 0) {
            return null;
        }
        return gson.fromJson(json, TYPE);
    }

    /**
     * for test
     *
     * @param args command line arguments
     * @since ostermillerutils 1.00.00
     */
    public static void main(String[] args) {

        String diif_data_path = "/Users/hedingxu/robust-github/Robust/app/resource_diff_data.robust_tmp";
        APKDiffData oldDiffdata = new APKDiffData();
        oldDiffdata.oldResourcesArscCrc = 3372233974L;
        oldDiffdata.diffTypeName = "apk";
        DataUnit dd = new DataUnit();
        dd.name = "resources.arsc";
        dd.newCrc = 2912363016L;
        dd.newMd5 = "e2a7e2d8469e1b5c2d4b5688bba80bc2";
        dd.diffMd5 = "a8e134e31c06122cfd651989f7759e30";
        dd.oldMd5 = "2a30f769a904a4d103939b83f295de6e";
        oldDiffdata.diffModSet.add(dd);

        writeDiffData(new File(diif_data_path), oldDiffdata);

        APKDiffData diffData = readDiffData(new File(diif_data_path));
        if (diffData == null) {
            System.err.println("blank ");
        }
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
        FileUtil.createFile(file.getAbsolutePath());

        TxtFileReaderAndWriter.writeFile(file, content);
        return true;
    }
}
