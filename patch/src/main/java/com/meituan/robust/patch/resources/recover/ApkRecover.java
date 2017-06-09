package com.meituan.robust.patch.resources.recover;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.os.Build;
import android.text.TextUtils;

import com.meituan.robust.common.MD5;
import com.meituan.robust.common.ResourceConstant;
import com.meituan.robust.patch.resources.APKStructure;
import com.meituan.robust.patch.resources.diff.ApkDiffDataReaderAndWriter;
import com.meituan.robust.patch.resources.diff.data.APKDiffData;

import java.io.BufferedInputStream;
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
        String wantedMd5 = ResourcesApkVerifyUtils.readResourcesApkMd5(robustResourcesApkMd5File);
        try {
            String realMd5 = MD5.getHashString(robustResourcesApkFile);
            if (TextUtils.equals(wantedMd5, realMd5)) {
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
        String resourcesDirPathString = getResourceDirPathString(context, patchName, patchMd5);
        String robustResourcesApkPath = resourcesDirPathString + File.separator + ROBUST_RESOURCES_APK;
        String robustResourcesApkMd5Path = resourcesDirPathString + File.separator + ROBUST_RESOURCES_APK_MD5;
        boolean isRecovered = isRecovered(robustResourcesApkPath, robustResourcesApkMd5Path);
        if (isRecovered) {
            return true;
        }

        String recoverResourceDir = resourcesDirPathString + File.separator + ROBUST_RESOURCE_DIR;
        File recoverResourceDirFile = new File(recoverResourceDir);
        RecoverUtils.cleanDir(recoverResourceDirFile);

        File apkDiffDataFile = new File(recoverResourceDir, APKDiffData.ROBUST_RESOURCES_DIFF_RELATIVE_PATH);
        ZipFile diffApkZipFile = null;
        try {
            diffApkZipFile = new ZipFile(tempPatchPath);
            ZipEntry apkDiffDataEntry = diffApkZipFile.getEntry(APKDiffData.ROBUST_RESOURCES_DIFF_RELATIVE_PATH);
            BufferedInputStream bis = new BufferedInputStream(diffApkZipFile.getInputStream(apkDiffDataEntry));
            FileOutputStream fos = new FileOutputStream(apkDiffDataFile);
            BufferedOutputStream out = new BufferedOutputStream(fos);

            try {
                byte[] buffer = new byte[ResourceConstant.BUFFER_SIZE];
                int length = bis.read(buffer);
                while (length != -1) {
                    out.write(buffer, 0, length);
                    length = bis.read(buffer);
                }
            } finally {
                closeQuietly(out);
                closeQuietly(bis);
            }

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        if (!apkDiffDataFile.exists()) {
            return false;
        }

        APKDiffData apkDiffData = ApkDiffDataReaderAndWriter.readDiffData(apkDiffDataFile);

        ApplicationInfo applicationInfo = context.getApplicationInfo();
        if (applicationInfo == null) {
            return false;
        }

        String baseApkFilePath = applicationInfo.sourceDir;

        if (TextUtils.isEmpty(baseApkFilePath)) {
            return false;
        }

        if (apkDiffData.isEmpty()) {
            return false;
        }

        ZipFile baseApkZipFile = null;
        try {
            baseApkZipFile = new ZipFile(baseApkFilePath);
        } catch (IOException e) {
            e.printStackTrace();
        }


        File robustResourcesApkFile = new File(robustResourcesApkPath);
        if (robustResourcesApkFile.exists()) {
            robustResourcesApkFile.delete();
        }

        ZipFile robustResourcesApkZipFile = null;
        try {
            robustResourcesApkZipFile = new ZipFile(robustResourcesApkFile);
        } catch (IOException e) {
            e.printStackTrace();
        }

        ZipOutputStream resourcesApkZipOutputStream = null;
        try {
            resourcesApkZipOutputStream = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(robustResourcesApkFile)));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        }

        //merge diff mod file ,and zip diff mod file to resources apk
        boolean resultDiffModSet = RecoverUtils.handleDiffModSet(context, baseApkZipFile, diffApkZipFile, robustResourcesApkZipFile, resourcesApkZipOutputStream, recoverResourceDirFile, apkDiffData);
        if (!resultDiffModSet) {
            return false;
        }

        //zip mod file to resources apk
        boolean resultModSet = RecoverUtils.handleOtherSet(context, baseApkZipFile, diffApkZipFile, robustResourcesApkZipFile, resourcesApkZipOutputStream, recoverResourceDirFile, apkDiffData);
        if (!resultModSet) {
            return false;
        }

        //zip diff data file to resources apk
        ZipEntry diffDataZipEntry = diffApkZipFile.getEntry(ResourceConstant.ROBUST_RESOURCES_DIFF_RELATIVE_PATH);
        try {
            RecoverUtils.zipEntry2ApkOutputStream(diffApkZipFile, diffDataZipEntry, resourcesApkZipOutputStream);
        } catch (Throwable throwable) {
            return false;
        }

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
                try {
                    RecoverUtils.zipEntry2ApkOutputStream(baseApkZipFile, zipEntry, resourcesApkZipOutputStream);
                } catch (Throwable throwable) {
                    return false;
                }
            }
        }

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
        closeZip(robustResourcesApkZipFile);

        try {
            String robustResourcesApkZipFileMd5 = MD5.getHashString(robustResourcesApkFile);

            if (!TextUtils.isEmpty(robustResourcesApkZipFileMd5)) {
                File robustResourcesApkMd5File = new File(robustResourcesApkMd5Path);
                boolean writeResult = ResourcesApkVerifyUtils.writeResourcesApkMd5(robustResourcesApkMd5File, robustResourcesApkZipFileMd5);
                if (writeResult) {
                    return writeResult;
                }
            }
        } catch (IOException e) {
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
