package robust.gradle.plugin

import java.util.zip.Deflater
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
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

        zos.putNextEntry(new ZipEntry(buildPath(path, file.getName())));

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

    def static void addFile2Zip(File zipFile, File robustHashFile) {
        def tempZipFile = new File(zipFile.name + "temp", zipFile.parentFile);
        if (tempZipFile.exists()) {
            tempZipFile.delete();
        }

        ZipInputStream zin = new ZipInputStream(new FileInputStream(zipFile))
        ZipOutputStream out = new ZipOutputStream(new FileOutputStream(tempZipFile))

        ZipEntry entry = zin.getNextEntry()
        byte[] buf = new byte[1024 * 8]
        while (entry != null) {
            String name = entry.getName()

            // Add ZIP entry to output stream.
            ZipEntry zipEntry = new ZipEntry(name);

            if (ZipEntry.STORED == entry.getMethod()) {
                zipEntry.setMethod(entry.getMethod())
                zipEntry.setSize(entry.getSize())
                zipEntry.setCompressedSize(entry.getCompressedSize())
                zipEntry.setCrc(entry.getCrc())
            }

            out.putNextEntry(zipEntry);

            int len
            while ((len = zin.read(buf)) > 0) {
                out.write(buf, 0, len)
            }
            out.closeEntry()
            entry = zin.getNextEntry()
        }

        zin.close()

        RobustApkHashZipUtils.zipFile(out, "assets", robustHashFile)
        out.close()

        zipFile.delete()
        tempZipFile.renameTo(zipFile)
    }
}