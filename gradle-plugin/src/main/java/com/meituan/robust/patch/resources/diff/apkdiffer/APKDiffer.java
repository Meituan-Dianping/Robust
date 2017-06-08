package com.meituan.robust.patch.resources.diff.apkdiffer;


import com.meituan.robust.patch.resources.config.RobustResourceConfig;
import com.meituan.robust.patch.resources.diff.data.APKDiffData;
import com.meituan.robust.patch.resources.diff.data.BaseDiffData;
import com.meituan.robust.common.FileUtil;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class APKDiffer extends BaseDiffer {
    private List<BaseDiffer> differs = new ArrayList<>();

    public APKDiffer(RobustResourceConfig config) {
        super(config);
        differs.add(new AndroidManifestDiffer(config));
        differs.add(new ResourcesArscDiffer(config));
        differs.add(new ResDiffer(config));
        differs.add(new AssetsDiffer(config));
        differs.add(new LibDiffer(config));
        differs.add(new DexDiffer(config));
        differs.add(new METAINFDiff(config));
    }

    @Override
    public boolean diffNewFile(Path newFilePath) {
        for (BaseDiffer differ : differs) {
            differ.diffNewFile(newFilePath);
        }
        return true;
    }

    @Override
    public boolean diffOldFile(Path oldFilePath) {
        for (BaseDiffer differ : differs) {
            differ.diffOldFile(oldFilePath);
        }
        return true;
    }

    @Override
    public boolean isNeed(Path filePath) {
        return true;
    }

    public List<BaseDiffData> getDiffDatas() {
        List<BaseDiffData> diffDatas = new ArrayList<>();
        for (BaseDiffer differ : differs) {
            BaseDiffData diffData = differ.getDiffData();
            diffDatas.add(diffData);
        }
        return diffDatas;
    }

    private boolean unzipApkFile(File file, File destFile) throws IOException {
        String apkName = file.getName();
        if (!apkName.endsWith(RobustResourceConfig.APK_SUFFIX)) {
            return false;
        }

        String destPath = destFile.getAbsolutePath();
        FileUtil.deleteAllFile(destPath);
        unZipAPk(file.getAbsoluteFile().getAbsolutePath(), destPath);

        return true;
    }

    private boolean unzipApkFiles(File oldFile, File newFile) throws IOException{
        return unzipApkFile(oldFile, this.oldApkUnZipDir) &&
                unzipApkFile(newFile, this.newApkUnZipDir);
    }

    private static void unZipAPk(String fileName, String filePath) throws IOException {

        ZipFile zipFile = new ZipFile(fileName);
        Enumeration enumeration = zipFile.entries();
        try {
            while (enumeration.hasMoreElements()) {
                ZipEntry entry = (ZipEntry) enumeration.nextElement();
                if (entry.isDirectory()) {
                    new File(filePath, entry.getName()).mkdirs();
                    continue;
                }
                BufferedInputStream bis = new BufferedInputStream(zipFile.getInputStream(entry));

                File file = new File(filePath + File.separator + entry.getName());

                File parentFile = file.getParentFile();
                if (parentFile != null && (!parentFile.exists())) {
                    parentFile.mkdirs();
                }
                FileOutputStream fos = null;
                BufferedOutputStream bos = null;
                try {
                    fos = new FileOutputStream(file);
                    bos = new BufferedOutputStream(fos, RobustResourceConfig.BUFFER_SIZE);

                    byte[] buf = new byte[RobustResourceConfig.BUFFER_SIZE];
                    int len;
                    while ((len = bis.read(buf, 0, RobustResourceConfig.BUFFER_SIZE)) != -1) {
                        fos.write(buf, 0, len);
                    }
                } finally {
                    if (bos != null) {
                        bos.flush();
                        bos.close();
                    }
                    if (bis != null) {
                        bis.close();
                    }
                }
            }
        } finally {
            if (zipFile != null) {
                zipFile.close();
            }
        }
    }


    public APKDiffData diffAPK(File oldApkFile, File newApkFile) throws IOException {
        APKDiffData apkDiffData = new APKDiffData();

        List<BaseDiffData> diffDatas = new ArrayList<>();
        //todo hedex : if AndroidManifest.xml 's component changed, can not work with out component proxy
        boolean unzipResults = unzipApkFiles(oldApkFile, newApkFile);
        if (!unzipResults) {
            return apkDiffData;
        }
        HashSet<Path> visitedPathes = new HashSet<>();

        Files.walkFileTree(newApkUnZipDir.toPath(), new NewApkFileVisitor(config, newApkUnZipDir.toPath(), oldApkUnZipDir.toPath(), this, visitedPathes));

        Files.walkFileTree(oldApkUnZipDir.toPath(), new OldApkFileVisitor(config, newApkUnZipDir.toPath(), oldApkUnZipDir.toPath(), this, visitedPathes));

        diffDatas.addAll(getDiffDatas());

        //merge diff datas to apkDiffData
        for (BaseDiffData diffData : diffDatas) {
            apkDiffData.mergeDiffData(diffData);
        }

        //write old apk 's resources.arsc crc to apk diff data
        apkDiffData.oldResourcesArscCrc = new ZipFile(oldApkFile).getEntry("resources.arsc").getCrc();
        return apkDiffData;
    }

}
