package robust.gradle.plugin

import com.meituan.robust.Constants
import com.meituan.robust.common.CrcUtil
import com.meituan.robust.common.FileUtil

import java.util.zip.Deflater
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
/**
 * Created by hedex on 17/2/14.
 */
class RobustApkHashZipUtils {
    static void packZip(File output, List<File> sources) throws IOException {
        ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream(output));
        zipOut.setLevel(Deflater.BEST_SPEED);

        for (File source : sources) {
            if (source.isDirectory()) {
                zipDir(zipOut, "", source);
            } else {
                zipFile(zipOut, "", source);
            }
        }
        zipOut.flush();
        zipOut.close();
    }

    private static String buildPath(String path, String file) {
        if (path == null || path == "") {
            return file;
        } else {
            return path + "/" + file;
        }
    }

    private static void zipDir(ZipOutputStream zos, String path, File dir) throws IOException {
        if (!dir.canRead()) {
            return;
        }

        File[] files = dir.listFiles();
        path = buildPath(path, dir.getName());

        for (File source : files) {
            if (source.isDirectory()) {
                zipDir(zos, path, source);
            } else {
                zipFile(zos, path, source);
            }
        }

    }

    def static void zipFile(ZipOutputStream zos, String path, File file) throws IOException {
        if (!file.canRead()) {
            return;
        }

        zos.putNextEntry(new ZipEntry(buildPath(path, file.getAbsolutePath())));

        FileInputStream fis = new FileInputStream(file);

        byte[] buffer = new byte[4092];
        int byteCount = 0;
        while ((byteCount = fis.read(buffer)) != -1) {
            zos.write(buffer, 0, byteCount);
            System.out.flush();
        }

        fis.close();
        zos.closeEntry();
    }

    def static void addFile2Zip(File apFile, File robustHashFile) {
        String entryName = "assets/" + Constants.ROBUST_APK_HASH_FILE_NAME;
        ZipEntry zipEntry = new ZipEntry(entryName);
        zipEntry.setMethod(ZipEntry.STORED);
        zipEntry.setSize(robustHashFile.length());
        zipEntry.setCompressedSize(robustHashFile.length());
        zipEntry.setCrc(CrcUtil.computeFileCrc32(robustHashFile));

        ZipOutputStream zipOutputStream = new ZipOutputStream(new FileOutputStream(apFile))
        FileInputStream fileInputStream = new FileInputStream(robustHashFile);

        FileUtil.addZipEntry(zipOutputStream,zipEntry,fileInputStream)

        fileInputStream.close();
        zipOutputStream.close()
    }
}