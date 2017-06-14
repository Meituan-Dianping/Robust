package com.meituan.robust.patch.resources.recover;

import android.content.Context;
import android.util.Log;

import com.meituan.robust.common.FileUtil;
import com.meituan.robust.common.MD5;
import com.meituan.robust.common.ResourceConstant;
import com.meituan.robust.patch.resources.APKStructure;
import com.meituan.robust.patch.resources.diff.data.APKDiffData;
import com.meituan.robust.patch.resources.diff.data.DataUnit;
import com.meituan.robust.patch.resources.diff.util.DiffAndRecoverUtil;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;


/**
 * Created by hedingxu on 17/6/4.
 */

class RecoverUtils {

    private RecoverUtils() {

    }

    public static boolean copyPatch(String patchLocalPath, String destPatchPath) {
        boolean result = false;
        try {
            FileUtil.copyFile(patchLocalPath, destPatchPath);
            result = true;
        } catch (Throwable throwable) {

        }
        return result;
    }

    public static void cleanDir(File dir) {
        if (dir.exists()) {
            FileUtil.deleteAllFile(dir.getAbsolutePath());
            dir.mkdirs();
        }
    }


    public static boolean extract(ZipFile zipFile, ZipEntry entryFile, File extractTo, String targetMd5) throws IOException {
        int numAttempts = 0;
        boolean isExtractionSuccessful = false;
        while (numAttempts < 3 && !isExtractionSuccessful) {
            numAttempts++;
            BufferedInputStream bis = new BufferedInputStream(zipFile.getInputStream(entryFile));
            FileOutputStream fos = new FileOutputStream(extractTo);
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


            isExtractionSuccessful = MD5.getHashString(extractTo).equals(targetMd5);

            if (!isExtractionSuccessful) {
                extractTo.delete();
            }
        }

        return isExtractionSuccessful;
    }

    public static boolean extract(ZipFile zipFile, ZipEntry entryFile, File extractTo) throws IOException {
        int numAttempts = 0;
        boolean isExtractionSuccessful = false;
        while (numAttempts < 3 && !isExtractionSuccessful) {
            numAttempts++;
            BufferedInputStream bis = new BufferedInputStream(zipFile.getInputStream(entryFile));
            FileOutputStream fos = new FileOutputStream(extractTo);
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

            isExtractionSuccessful = true;
        }

        return isExtractionSuccessful;
    }

    public static boolean handleDiffModSet(Context context, ZipFile baseApkFile, ZipFile diffApkFile, ZipOutputStream robustResourcesApkZipOutputStream, File recoverResourceDirFile,
                                           APKDiffData apkDiffData) {
        try {
            ZipEntry resourcesArscEntry = baseApkFile.getEntry(APKStructure.ResourcesArsc_Type);
            if (resourcesArscEntry == null) {
                return false;
            }
            long baseArscCrc = resourcesArscEntry.getCrc();
            if (baseArscCrc != apkDiffData.oldResourcesArscCrc) {
                Log.e("Robust", "RecoverUtils handleDiffModSet 124 arsc :" + baseArscCrc +", patch base arsc: " + apkDiffData.oldResourcesArscCrc);
//                System.err.println("arsc :" + baseArscCrc +", patch base arsc: " + apkDiffData.oldResourcesArscCrc);
                return false;
            }

            if (apkDiffData.diffModSet.isEmpty()) {
                return true;
            }

            for (DataUnit dataUnit : apkDiffData.diffModSet) {

                String name = dataUnit.name;

                File recoverFile = new File(recoverResourceDirFile, name);

                ZipEntry diffEntry = diffApkFile.getEntry(name);
                if (diffEntry == null) {
                    continue;
                }

                ZipEntry baseEntry = baseApkFile.getEntry(name);
                if (baseEntry == null) {
                    continue;
                }

                InputStream oldStream = null;
                InputStream diffStream = null;
                try {
                    oldStream = baseApkFile.getInputStream(baseEntry);
                    diffStream = diffApkFile.getInputStream(diffEntry);
                    DiffAndRecoverUtil.recover(oldStream, diffStream, recoverFile);
                } finally {
                    closeQuietly(oldStream);
                    closeQuietly(diffStream);
                }

                if (recoverFile.exists() && MD5.getHashString(recoverFile).equals(dataUnit.newMd5)) {
                    //write to resourcesApkFile
                    Log.d("Robust", "RecoverUtils zipBigFile2ApkOutputStream 161 name: " + dataUnit.name);
                    zipBigFile2ApkOutputStream(dataUnit.name, recoverFile, dataUnit.newCrc, robustResourcesApkZipOutputStream);
                    return true;
                } else {
                    return false;
                }
            }
        } catch (Throwable e) {
            Log.d("Robust", "RecoverUtils zipBigFile2ApkOutputStream Throwable 161 : " + e.toString());
        }
        return true;
    }


    private static void zipBigFile2ApkOutputStream(String entryName, File bigFile, long bigFileCrc, ZipOutputStream robustResourcesApkZipOutputStream) throws IOException {
        ZipEntry zipEntry = new ZipEntry(entryName);

        zipEntry.setMethod(ZipEntry.STORED);
        zipEntry.setSize(bigFile.length());
        zipEntry.setCompressedSize(bigFile.length());
        zipEntry.setCrc(bigFileCrc);
        FileInputStream in = null;
        try {
            in = new FileInputStream(bigFile);
            FileUtil.addZipEntry(robustResourcesApkZipOutputStream, zipEntry, in);
        } catch (Exception e) {

        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    public static boolean handleOtherSet(Context context, ZipFile baseApkZipFile, ZipFile diffApkZipFile, ZipOutputStream robustResourcesApkZipOutputStream, File recoverResourceDirFile, APKDiffData apkDiffData) {
        Log.d("Robust", "RecoverUtils handleOtherSet 202 ");
        if (!apkDiffData.addSet.isEmpty()) {
            for (DataUnit data : apkDiffData.addSet) {
                String name = data.name;
                Log.d("Robust", "RecoverUtils apkDiffData.addSet 205 name: " + name);
                ZipEntry zipEntry = diffApkZipFile.getEntry(name);
                if (name.startsWith(APKStructure.Lib_Type)) {
                    File newLibFile = new File(recoverResourceDirFile, name);
                    try {
                        RecoverUtils.extract(diffApkZipFile, zipEntry, newLibFile, data.newMd5);
                    } catch (Throwable throwable) {
                        Log.e("Robust", "RecoverUtils apkDiffData.addSet 213 Throwable: " + throwable.toString());
                        throwable.printStackTrace();
                        return false;
                    }
                } else {
                    try {
                        FileUtil.addZipEntry(robustResourcesApkZipOutputStream, new ZipEntry(zipEntry.getName()), diffApkZipFile.getInputStream(zipEntry));
                    } catch (Throwable throwable) {
                        Log.e("Robust", "RecoverUtils apkDiffData.addSet 221 Throwable: " + throwable.toString());
                        throwable.printStackTrace();
                        return false;
                    }
                }

            }
        }

        if (!apkDiffData.modSet.isEmpty()) {
            for (DataUnit data : apkDiffData.modSet) {
                String name = data.name;
                Log.d("Robust", "RecoverUtils apkDiffData.modSet 233 name: " + name);
                ZipEntry zipEntry = diffApkZipFile.getEntry(name);
                if (name.startsWith(APKStructure.Lib_Type)) {
                    File newLibFile = new File(recoverResourceDirFile, name);
                    try {
                        RecoverUtils.extract(diffApkZipFile, zipEntry, newLibFile, data.newMd5);
                    } catch (Throwable throwable) {
                        Log.e("Robust", "RecoverUtils apkDiffData.modSet 240 Throwable: " + throwable.toString());
                        throwable.printStackTrace();
                        return false;
                    }
                } else {
                    try {
                        FileUtil.addZipEntry(robustResourcesApkZipOutputStream, new ZipEntry(zipEntry.getName()), diffApkZipFile.getInputStream(zipEntry));
                    } catch (Throwable throwable) {
                        Log.e("Robust", "RecoverUtils apkDiffData.modSet 248 Throwable: " + throwable.toString());
                        throwable.printStackTrace();
                        return false;
                    }
                }
            }
        }

//        if (!apkDiffData.delSet.isEmpty()) {
//            for (DataUnit data : apkDiffData.delSet) {
//
//            }
//
//        }

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

}
