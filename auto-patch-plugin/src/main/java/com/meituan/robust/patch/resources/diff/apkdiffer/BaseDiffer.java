package com.meituan.robust.patch.resources.diff.apkdiffer;

import com.meituan.robust.common.CrcUtil;
import com.meituan.robust.common.FileUtil;
import com.meituan.robust.common.MD5;
import com.meituan.robust.patch.resources.APKStructure;
import com.meituan.robust.patch.resources.config.RobustResourceConfig;
import com.meituan.robust.patch.resources.diff.data.BaseDiffData;
import com.meituan.robust.patch.resources.diff.data.DataUnit;
import com.meituan.robust.patch.resources.diff.util.DiffAndRecoverUtil;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Iterator;
import java.util.regex.Pattern;
/**
 * Created by hedingxu on 17/5/31.
 */
public class BaseDiffer {

    protected final RobustResourceConfig config;
    protected final File outDir;

    protected final File resultDir;

    //unzip的目录
    protected final File oldApkUnZipDir;
    protected final File newApkUnZipDir;

    protected final File oldApkFile;
    protected final File newApkFile;

    protected HashSet<Pattern> includePatterns;
    protected HashSet<Pattern> excludePatterns;

    protected String apkResourceType = "";

    protected BaseDiffData diffData;

    protected static int DIFF_LIMIT_SIZE = 200 * 1024;//200kb

    public BaseDiffer(RobustResourceConfig config) {
        this.config = config;
        this.outDir = new File(config.robustOutputsFolder);

        this.resultDir = config.apkDiffOutDir;

        this.oldApkFile = config.oldApkFile;
        this.newApkFile = config.newApkFile;

        this.oldApkUnZipDir = config.oldApkUnZipDir;
        this.newApkUnZipDir = config.newApkUnZipDir;
        if (config.bigFileSizeAtLeast > 0) {
            DIFF_LIMIT_SIZE = config.bigFileSizeAtLeast;
        }
    }

    public RobustResourceConfig getConfig() {
        return config;
    }

    protected BaseDiffData getDiffData() {
        return diffData;
    }

    public Path getRelativePath(File file) {
        return config.newApkUnZipDir.toPath().relativize(file.toPath());
    }

    private Path getApkDiffOutPathFromNewFile(File newFile) {
        return config.apkDiffOutDir.toPath().resolve(getRelativePath(newFile));
    }

    public String getRelativePathStringToOldFile(File oldFile) {
        return config.oldApkUnZipDir.toPath().relativize(oldFile.toPath()).toString();
    }

    public Path getRelativePathToOldFile(File oldFile) {
        return config.oldApkUnZipDir.toPath().relativize(oldFile.toPath());
    }

    public Path getRelativePathToNewFile(File newFile) {
        return config.newApkUnZipDir.toPath().relativize(newFile.toPath());
    }

    public String getRelativePathStringToNewFile(File newFile) {
        return config.newApkUnZipDir.toPath().relativize(newFile.toPath()).toString();
    }

    public String getParentRelativePathStringToNewFile(File newFile) {
        return config.newApkUnZipDir.toPath().relativize(newFile.getParentFile().toPath()).toString().replace("\\", "/");
    }

    /**
     * @param newFilePath
     */
    protected boolean diffNewFile(Path newFilePath) {
        Path newApkPath = newApkUnZipDir.toPath();
        Path oldApkPath = oldApkUnZipDir.toPath();

        File oldFile = null;
        File newFile = null;

        newFile = new File(newApkUnZipDir,newFilePath.toString());

        //new File 相对路径
        Path relativePath = newFilePath;

        //对应的old File路径
        Path oldPath = oldApkPath.resolve(relativePath);

        oldFile = oldPath.toFile();

        try {
            diff(oldFile, newFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    /**
     * @param oldFilePath
     */
    protected boolean diffOldFile(Path oldFilePath) {
        Path newApkPath = newApkUnZipDir.toPath();
        Path oldApkPath = oldApkUnZipDir.toPath();

        File oldFile = null;
        File newFile = null;


        oldFile = new File(oldApkUnZipDir,oldFilePath.toString());
//        newFile = newFilePath.toFile();

        //new File 相对路径
//        Path relativePath = newApkPath.relativize(newFilePath);
        Path relativePath = oldFilePath;
        //对应的old File路径
//        Path oldPath = oldApkPath.resolve(relativePath);
        Path newPath = newApkPath.resolve(relativePath);

//        oldFile = oldPath.toFile();
        newFile = newPath.toFile();

        try {
            diff(oldFile, newFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    protected boolean isNeed(Path filePath) {
        Path relativeFilePath = filePath;

        if (checkPatterns(excludePatterns, relativeFilePath)) {
            return false;
        }
        if (checkPatterns(includePatterns, relativeFilePath)) {
            return true;
        }
        return false;
    }

    protected boolean isNeedDiff(File file) {
        long length = file.length();
        if (length > DIFF_LIMIT_SIZE) {
            return true;
        }
        return false;
    }

    public static boolean checkPatterns(HashSet<Pattern> patterns, Path relativePath) {

        //兼容 linux mac windows
        if (!patterns.isEmpty()) {
            for (Iterator<Pattern> it = patterns.iterator(); it.hasNext(); ) {
                Pattern p = it.next();
                String linux_mac_key = relativePath.toString().replace("\\", "/");
                String windows_key = relativePath.toString().replace("/", "\\");
                if (p.matcher(linux_mac_key).matches() || p.matcher(windows_key).matches()) {
                    return true;
                }
            }
        }
        return false;
    }

    protected boolean checkResourceType(Path relativeFilePath) {
        String linux_mac_key = relativeFilePath.toString().replace("\\", "/");
        String windows_key = relativeFilePath.toString().replace("/", "\\");
        if (linux_mac_key.startsWith(apkResourceType) || windows_key.startsWith(apkResourceType)) {
            return true;
        } else {
            return false;
        }
    }

    protected boolean diff(File oldFile, File newFile) throws IOException {
        if (null == newFile || !newFile.exists() || newFile.isDirectory()) {
            if (null == oldFile || !oldFile.exists() || oldFile.isDirectory()) {
                return false;
            }
        }

        // old file is deleted
        if (null == newFile || !newFile.exists() || newFile.isDirectory()) {
            //do not copy file to apk diff dir
            DataUnit dataUnit = new DataUnit();
            dataUnit.name = getRelativePathStringToOldFile(oldFile);
            dataUnit.oldMd5 = MD5.getHashString(oldFile);
            dataUnit.newMd5 = "";
            dataUnit.diffMd5 = "";
            diffData.delSet.add(dataUnit);
            return true;
        }

        //new file is added
        if (null == oldFile || !oldFile.exists() || oldFile.isDirectory()) {
            //copy new file to resources diff dir
            File diffFile = getApkDiffOutPathFromNewFile(newFile).toFile();
            FileUtil.copyFile(newFile, diffFile);

            DataUnit dataUnit = new DataUnit();
            dataUnit.name = getRelativePathStringToNewFile(newFile);
            dataUnit.oldMd5 = "";
            dataUnit.newMd5 = MD5.getHashString(diffFile);
            dataUnit.diffMd5 = MD5.getHashString(diffFile);
            diffData.addSet.add(dataUnit);
            return true;
        }

        //old file modify to new file
        if (oldFile.length() == 0 && newFile.length() == 0) {
            return false;
        }

        String newMd5 = MD5.getHashString(newFile);
        String oldMd5 = MD5.getHashString(oldFile);

        if (null == newMd5 || null == oldMd5 || "".equals(newMd5) || "".equals(oldMd5)) {
            return false;
        }

        //文件没有变更
        if (newMd5.equals(oldMd5)) {
            return false;
        }

        File diffFile = getApkDiffOutPathFromNewFile(newFile).toFile();

        //todo support lib(so) use diff-tool
        boolean isLibFile = getRelativePathStringToNewFile(newFile).startsWith(APKStructure.Lib_Type);

        if (false == isLibFile && isNeedDiff(newFile)) {
            if (!diffFile.getParentFile().exists()) {
                diffFile.getParentFile().mkdirs();
            }
            DiffAndRecoverUtil.diff(oldFile, newFile, diffFile);

            long diffLengthLimited = newFile.length() * 3 / 5;//60%的条件

            if (diffFile.exists() && diffFile.length() < diffLengthLimited) {
                DataUnit dataUnit = new DataUnit();
                dataUnit.name = getRelativePathStringToNewFile((newFile));
                dataUnit.oldMd5 = MD5.getHashString(oldFile);
                dataUnit.newMd5 = MD5.getHashString(newFile);
                dataUnit.diffMd5 = MD5.getHashString(diffFile);
                dataUnit.newCrc = CrcUtil.computeFileCrc32(newFile);
                diffData.diffModSet.add(dataUnit);
                return true;
            } else {

            }
        }

        //这里的difffile有可能是diff 之后不满足60%这个条件的
        if (diffFile.exists()) {
            diffFile.delete();
        }
        //这里的 diffFile = newFile
        FileUtil.copyFile(newFile, diffFile);

        DataUnit dataUnit = new DataUnit();
        dataUnit.name = getRelativePathStringToNewFile((newFile));
        dataUnit.oldMd5 = MD5.getHashString(oldFile);
        dataUnit.newMd5 = MD5.getHashString(diffFile);
        dataUnit.diffMd5 = MD5.getHashString(diffFile);
        diffData.modSet.add(dataUnit);
        return true;
    }
}
