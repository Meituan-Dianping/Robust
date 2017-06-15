package com.meituan.robust.patch.resources.recover;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import com.meituan.robust.common.FileUtil;
import com.meituan.robust.common.MD5;
import com.meituan.robust.common.ResourceConstant;
import com.meituan.robust.common.TxtFileReaderAndWriter;
import com.meituan.robust.patch.resources.APKStructure;
import com.meituan.robust.patch.resources.diff.ApkDiffDataReaderAndWriter;
import com.meituan.robust.patch.resources.diff.data.APKDiffData;

import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;


/**
 * Created by hedingxu on 17/6/6.
 */
public class ApkRecover {

    public static final String ROBUST_RESOURCES_APK = "robust_resources.apk";
    public static final String ROBUST_RESOURCES_APK_MD5 = "robust_resources_apk.md5";
    public static final String ROBUST_PATCH_TEMP = "patch_temp.apk";
    public static final String ROBUST_RESOURCE_DIR = "merge_dir";

    //  files/patch/robust/resource/name_md5 / { patch_temp.apk robust_resources.apk}
    public static String getResourceDirPathString(Context context, String patchName, String patchMd5) {
        String resourcesDirPathString = context.getFilesDir().getAbsolutePath() + File.separator + "patch" + File.separator + "robust" + File.separator + "resource" + File.separator + patchName + "_" + patchMd5;
        return resourcesDirPathString;
    }


    private static boolean isRecovered(String robustResourcesApkPath, String robustResourcesApkMd5Path) {
        File robustResourcesApkMd5File = new File(robustResourcesApkMd5Path);
        File robustResourcesApkFile = new File(robustResourcesApkPath);
        if (null == robustResourcesApkFile || null == robustResourcesApkMd5File) {
            return false;
        }
        if (!robustResourcesApkFile.exists() || !robustResourcesApkMd5File.exists()) {
            return false;
        }
        String wantedMd5 = TxtFileReaderAndWriter.readFileAsString(robustResourcesApkMd5File);
        try {
            String realMd5 = MD5.getHashString(robustResourcesApkFile);
            if (wantedMd5.startsWith(realMd5)) {
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean isRecovered(Context context, String patchName, String patchMd5) {
        String resourcesDirPathString = getResourceDirPathString(context, patchName, patchMd5);
        String robustResourcesApkPath = resourcesDirPathString + File.separator + ROBUST_RESOURCES_APK;
        String robustResourcesApkMd5Path = resourcesDirPathString + File.separator + ROBUST_RESOURCES_APK_MD5;
        boolean isRecovered = isRecovered(robustResourcesApkPath, robustResourcesApkMd5Path);
        if (isRecovered) {
            return true;
        } else {
            return false;
        }
    }

    public static String getRobustResourcesApkPath(Context context, String patchName, String patchMd5) {
        String resourcesDirPathString = getResourceDirPathString(context, patchName, patchMd5);
        String robustResourcesApkPath = resourcesDirPathString + File.separator + ROBUST_RESOURCES_APK;
        return robustResourcesApkPath;
    }

    public static File getRobustResourcesMergeDirLibFile(Context context, String patchName, String patchMd5) {
        String resourcesDirPathString = getResourceDirPathString(context, patchName, patchMd5);
        String recoverResourceDir = resourcesDirPathString + File.separator + ROBUST_RESOURCE_DIR;
        File recoverResourceDirFile = new File(recoverResourceDir);
        File libDirFile = new File(recoverResourceDirFile, APKStructure.Lib_Type);
        return libDirFile;
    }

    public static String copyPatch2TmpPath(Context context, String patchName, String patchMd5, String patchPath) {
        String resourcesDirPathString = getResourceDirPathString(context, patchName, patchMd5);
        String tempPatchPath = resourcesDirPathString + File.separator + ROBUST_PATCH_TEMP;
        boolean result = RecoverUtils.copyPatch(patchPath, tempPatchPath);
        if (result) {
            return tempPatchPath;
        } else {
            return null;
        }
    }

    //recover : 资源patch + base.apk -> resources.apk
    public static synchronized boolean recover(Context context, String patchName, String patchMd5, String tempPatchPath) {
        Log.d("robust", "apkRecover recover 106");
        String resourcesDirPathString = getResourceDirPathString(context, patchName, patchMd5);
        String robustResourcesApkPath = resourcesDirPathString + File.separator + ROBUST_RESOURCES_APK;
        String robustResourcesApkMd5Path = resourcesDirPathString + File.separator + ROBUST_RESOURCES_APK_MD5;
        boolean isRecovered = isRecovered(robustResourcesApkPath, robustResourcesApkMd5Path);
        if (isRecovered) {
            Log.d("robust", "recover isRecovered 112:" + isRecovered);
            return true;
        }

        String recoverResourceDir = resourcesDirPathString + File.separator + ROBUST_RESOURCE_DIR;
        File recoverResourceDirFile = new File(recoverResourceDir);
        Log.d("robust", "recover cleanDir 118:" + recoverResourceDirFile);
        RecoverUtils.cleanDir(recoverResourceDirFile);


        File apkDiffDataFile = new File(recoverResourceDir, APKDiffData.ROBUST_RESOURCES_DIFF_RELATIVE_PATH);
        FileUtil.createFile(apkDiffDataFile.getAbsolutePath());
        ZipFile diffApkZipFile = null;
        try {
            diffApkZipFile = new ZipFile(tempPatchPath);
            Log.d("robust", "recover diffApkZipFile 126:" + tempPatchPath);
            ZipEntry apkDiffDataEntry = diffApkZipFile.getEntry(APKDiffData.ROBUST_RESOURCES_DIFF_RELATIVE_PATH);
            Log.d("robust", "recover apkDiffDataEntry 129:" + apkDiffDataEntry.getName());
            RecoverUtils.extract(diffApkZipFile, apkDiffDataEntry, apkDiffDataFile);

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        if (!apkDiffDataFile.exists()) {
            Log.d("robust", "recover apkDiffDataFile exists 138:" + apkDiffDataFile.exists());
            return false;
        }

        APKDiffData apkDiffData = ApkDiffDataReaderAndWriter.readDiffData(apkDiffDataFile);

        ApplicationInfo applicationInfo = context.getApplicationInfo();
        if (applicationInfo == null) {
            return false;
        }

        String baseApkFilePath = applicationInfo.sourceDir;

        if (TextUtils.isEmpty(baseApkFilePath)) {
            Log.e("robust", "recover baseApkFilePath  is isEmpty 152");
            return false;
        }

        Log.d("robust", "recover baseApkFilePath  155:" + baseApkFilePath);

        if (null == apkDiffData || apkDiffData.isEmpty()) {
            Log.e("Robust", "apkDiffData is blank");
            return false;
        }

        ZipFile baseApkZipFile = null;
        try {
            baseApkZipFile = new ZipFile(baseApkFilePath);
        } catch (IOException e) {
            Log.e("Robust", "baseApkZipFile 167 IOException: " + e.toString());
            e.printStackTrace();
        }


        File robustResourcesApkFile = new File(robustResourcesApkPath);
        if (robustResourcesApkFile.exists()) {
            robustResourcesApkFile.delete();
        }

        FileUtil.createFile(robustResourcesApkFile.getAbsolutePath());

        ZipOutputStream resourcesApkZipOutputStream = null;
        try {
            resourcesApkZipOutputStream = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(robustResourcesApkFile)));
        } catch (FileNotFoundException e) {
            Log.e("Robust", "FileNotFoundException 183 FileNotFoundException: " + e.toString());
            e.printStackTrace();
            return false;
        }

        //merge diff mod file ,and zip diff mod file to resources apk
        boolean resultDiffModSet = RecoverUtils.handleDiffModSet(context, baseApkZipFile, diffApkZipFile, resourcesApkZipOutputStream, recoverResourceDirFile, apkDiffData);
        Log.d("Robust", "handleDiffModSet 190 resultDiffModSet: " + resultDiffModSet);
        if (!resultDiffModSet) {
            return false;
        }

        //zip mod file to resources apk
        boolean resultModSet = RecoverUtils.handleOtherSet(context, baseApkZipFile, diffApkZipFile, resourcesApkZipOutputStream, recoverResourceDirFile, apkDiffData);
        Log.d("Robust", "handleOtherSet 197 resultModSet: " + resultModSet);
        if (!resultModSet) {
            return false;
        }

        Log.d("Robust", "zip diff data file to resources apk 201 ");
        //zip diff data file to resources apk
        ZipEntry diffDataZipEntry = diffApkZipFile.getEntry(ResourceConstant.ROBUST_RESOURCES_DIFF_RELATIVE_PATH);
        try {
            FileUtil.addZipEntry(resourcesApkZipOutputStream, new ZipEntry(diffDataZipEntry.getName()), diffApkZipFile.getInputStream(diffDataZipEntry));
        } catch (Throwable throwable) {
            Log.e("Robust", "addZipEntry 208 Throwable: " + throwable.toString());
            throwable.printStackTrace();
            return false;
        }

        Log.d("Robust", "zip other old files to resources apk 213 ");
        //zip other old files to resources apk
        final Enumeration<? extends ZipEntry> entries = baseApkZipFile.entries();
        while (entries.hasMoreElements()) {
            ZipEntry zipEntry = entries.nextElement();
            if (zipEntry == null) {
                return false;
            }
            String name = zipEntry.getName();
            if (name.contains("../")) {
                continue;
            }

            //classes/d{0,}.dex is no need
            if (!name.startsWith(APKStructure.Dex_Type) && !apkDiffData.isContains(name)) {
                Log.d("Robust", "FileUtil.addZipEntry to resources apk 228 : " + name);
                try {
                    FileUtil.addZipEntry(resourcesApkZipOutputStream, new ZipEntry(zipEntry.getName()), baseApkZipFile.getInputStream(zipEntry));
                } catch (Throwable throwable) {
                    Log.e("Robust", "addZipEntry throwable 231: " + throwable.toString());
                    return false;
                }
            }
        }

        Log.d("Robust", "copy comment(maybe contains channel info) to resources apk 237 ");
        // copy comment(maybe contains channel info) to resources apk
        String comment = "";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            comment = baseApkZipFile.getComment();
        } else {
            comment = ZipCommentUtil.getZipFileComment(baseApkFilePath);
        }

        if (!TextUtils.isEmpty(comment)) {
            resourcesApkZipOutputStream.setComment(comment);
        }

        closeQuietly(resourcesApkZipOutputStream);

        closeZip(baseApkZipFile);
        closeZip(diffApkZipFile);

        try {
            String robustResourcesApkZipFileMd5 = MD5.getHashString(robustResourcesApkFile);
            Log.d("Robust", "MD5.getHashString(robustResourcesApkFile) 258 : " + robustResourcesApkZipFileMd5);
            if (!TextUtils.isEmpty(robustResourcesApkZipFileMd5)) {
                File robustResourcesApkMd5File = new File(robustResourcesApkMd5Path);
                Log.d("Robust", "writeFile(robustResourcesApkMd5File, robustResourcesApkZipFileMd5) 261 : " + robustResourcesApkZipFileMd5);
                TxtFileReaderAndWriter.writeFile(robustResourcesApkMd5File, robustResourcesApkZipFileMd5);
                return true;
            }
        } catch (IOException e) {
            Log.e("Robust", "write Md5 IOException 266 :  " + e.toString());
            e.printStackTrace();
            return false;
        }

        return true;
    }

    private static void closeQuietly(Closeable closeable) {
        try {
            if (closeable != null) {
                closeable.close();
            }
        } catch (IOException e) {
        }
    }

    private static void closeZip(ZipFile zipFile) {
        try {
            if (zipFile != null) {
                zipFile.close();
            }
        } catch (IOException e) {
        }
    }


}
