package robust.gradle.plugin

import com.meituan.robust.Constants

import java.util.zip.*

/**
 * Created by hedex on 17/2/14.
 */
class RobustApkHashZipUtils {
    static void packZip(File output, List<File> sources) throws IOException {
        ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream(output));
        zipOut.setLevel(Deflater.BEST_SPEED);

        List<File> fileList = new LinkedList<File>();
        for (Object source : sources) {
            if (source instanceof File){
                fileList.add(source)
            } else if (source instanceof Collection){
                fileList.addAll(source)
            } else {
                System.err.println("packZip source 4" + source.getClass())
            }
        }

        for (File source : fileList) {
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

    def static void addApkHashFile2ApFile(File apFile, File robustHashFile) {
        def tempZipFile = new File(apFile.name + "temp", apFile.parentFile);
        if (tempZipFile.exists()) {
            tempZipFile.delete();
        }

        ZipOutputStream zipOutputStream = new ZipOutputStream(new FileOutputStream(tempZipFile))

        //copy ap file
        ZipFile apZipFile = new ZipFile(apFile)
        final Enumeration<? extends ZipEntry> entries = apZipFile.entries();
        while (entries.hasMoreElements()) {
            ZipEntry originZipEntry = entries.nextElement();
            ZipEntry rightZipEntry = getRightZipEntry(originZipEntry);
            if (null != rightZipEntry) {
                addZipEntry(zipOutputStream, rightZipEntry, apZipFile.getInputStream(originZipEntry))
            }
        }

        //add hash file
        String entryName = "assets/" + Constants.ROBUST_APK_HASH_FILE_NAME;
        ZipEntry zipEntry = new ZipEntry(entryName);
        zipEntry.setMethod(ZipEntry.STORED);
        zipEntry.setSize(robustHashFile.length());
        zipEntry.setCompressedSize(robustHashFile.length());
        zipEntry.setCrc(computeFileCrc32(robustHashFile));

        FileInputStream hashFileInputStream = new FileInputStream(robustHashFile);

        addZipEntry(zipOutputStream, zipEntry, hashFileInputStream)

        hashFileInputStream.close();
        zipOutputStream.close()

        apFile.delete()
        tempZipFile.renameTo(apFile.getAbsolutePath())
    }

    private static ZipEntry getRightZipEntry(ZipEntry originZipEntry){
        ZipEntry rightZipEntry = new ZipEntry(originZipEntry.getName());
        if (ZipEntry.STORED == originZipEntry.getMethod()) {
            rightZipEntry.setMethod(ZipEntry.STORED)
            rightZipEntry.setSize(originZipEntry.getSize())
            rightZipEntry.setCompressedSize(originZipEntry.getCompressedSize())
            rightZipEntry.setCrc(originZipEntry.getCrc())
        } else {
            rightZipEntry.setMethod(ZipEntry.DEFLATED)
        }
        if (originZipEntry.getComment() != null) {
            rightZipEntry.setComment(originZipEntry.getComment())
        }
        if (originZipEntry.getCreationTime() != null) {
            rightZipEntry.setCreationTime(originZipEntry.getCreationTime())
        }
        if (originZipEntry.getLastAccessTime() != null) {
            rightZipEntry.setLastAccessTime(originZipEntry.getLastAccessTime())
        }
        if (originZipEntry.getLastModifiedTime() != null) {
            rightZipEntry.setLastModifiedTime(originZipEntry.getLastModifiedTime())
        }
        if (originZipEntry.getTime() > 0) {
            rightZipEntry.setTime(originZipEntry.getTime())
        }
        return rightZipEntry;
    }

    private static long computeFileCrc32(File file) throws IOException {
        InputStream inputStream = new FileInputStream(file);
        CRC32 crc = new CRC32();
        int index;
        while ((index = inputStream.read()) != -1) {
            crc.update(index);
        }
        return crc.getValue();
    }

    /**
     * add zip entry
     *
     * @param zipOutputStream
     * @param zipEntry
     * @param inputStream
     * @throws Exception
     */
    private
    static void addZipEntry(ZipOutputStream zipOutputStream, ZipEntry zipEntry, InputStream inputStream) throws Exception {
        try {
            zipOutputStream.putNextEntry(zipEntry);
            byte[] buffer = new byte[1024];
            int length = -1;
            while ((length = inputStream.read(buffer, 0, buffer.length)) != -1) {
                zipOutputStream.write(buffer, 0, length);
                zipOutputStream.flush();
            }
        } catch (ZipException e) {
            // do nothing
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
            zipOutputStream.closeEntry();
        }
    }
}