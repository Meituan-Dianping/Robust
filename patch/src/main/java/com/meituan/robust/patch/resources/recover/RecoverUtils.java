package com.meituan.robust.patch.resources.recover;

import android.content.Context;

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

    private RecoverUtils(){

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

    public static boolean handleDiffModSet(Context context, ZipFile baseApkFile, ZipFile diffApkFile, ZipFile resourcesApkFile, ZipOutputStream robustResourcesApkZipOutputStream, File recoverResourceDirFile,
                                           APKDiffData apkDiffData) {
        try {
            ZipEntry resourcesArscEntry = baseApkFile.getEntry(APKStructure.ResourcesArsc_Type);
            if (resourcesArscEntry == null) {
                return false;
            }
            String baseArscCrc = String.valueOf(resourcesArscEntry.getCrc());
            if (!baseArscCrc.equals(apkDiffData.oldResourcesArscCrc)) {
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
                    zipBigFile2ApkOutputStream(dataUnit.name, recoverFile, dataUnit.newCrc, robustResourcesApkZipOutputStream);
                    return true;
                } else {
                    return false;
                }
            }
        } catch (Throwable e) {
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
            robustResourcesApkZipOutputStream.putNextEntry(new ZipEntry(zipEntry));
            byte[] buffer = new byte[ResourceConstant.BUFFER_SIZE];

            for (int length = in.read(buffer); length != -1; length = in.read(buffer)) {
                robustResourcesApkZipOutputStream.write(buffer, 0, length);
            }
            robustResourcesApkZipOutputStream.closeEntry();
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


    public static boolean handleOtherSet(Context context, ZipFile baseApkZipFile, ZipFile diffApkZipFile, ZipFile robustResourcesApkZipFile, ZipOutputStream robustResourcesApkZipOutputStream, File recoverResourceDirFile, APKDiffData apkDiffData) {
        if (!apkDiffData.addSet.isEmpty()) {
            for (DataUnit data : apkDiffData.addSet) {
                String name = data.name;
                ZipEntry zipEntry = diffApkZipFile.getEntry(name);
                if (name.startsWith(APKStructure.Lib_Type)) {
                    File newLibFile = new File(recoverResourceDirFile, name);
                    try {
                        RecoverUtils.extract(diffApkZipFile, zipEntry, newLibFile, data.newMd5);
                    } catch (Throwable throwable) {
                        return false;
                    }
                } else {
                    try {
                        RecoverUtils.zipEntry2ApkOutputStream(diffApkZipFile, zipEntry, robustResourcesApkZipOutputStream);
                    } catch (Throwable throwable) {
                        return false;
                    }
                }

            }
        }

        if (!apkDiffData.modSet.isEmpty()) {
            for (DataUnit data : apkDiffData.modSet) {
                String name = data.name;
                ZipEntry zipEntry = diffApkZipFile.getEntry(name);
                if (name.startsWith(APKStructure.Lib_Type)) {
                    File newLibFile = new File(recoverResourceDirFile, name);
                    try {
                        RecoverUtils.extract(diffApkZipFile, zipEntry, newLibFile, data.newMd5);
                    } catch (Throwable throwable) {
                        return false;
                    }
                } else {
                    try {
                        RecoverUtils.zipEntry2ApkOutputStream(diffApkZipFile, zipEntry, robustResourcesApkZipOutputStream);
                    } catch (Throwable throwable) {
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

    public static void zipEntry2ApkOutputStream(ZipFile zipFile, ZipEntry zipEntry, ZipOutputStream outputStream) throws IOException {
        InputStream in = null;
        try {
            in = zipFile.getInputStream(zipEntry);
            outputStream.putNextEntry(new ZipEntry(zipEntry));
            byte[] buffer = new byte[ResourceConstant.BUFFER_SIZE];

            for (int length = in.read(buffer); length != -1; length = in.read(buffer)) {
                outputStream.write(buffer, 0, length);
            }
            outputStream.closeEntry();
        } finally {
            if (in != null) {
                in.close();
            }
        }
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
