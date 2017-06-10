package com.meituan.robust.common;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipOperation {
    private static final int BUFFER_SIZE = 8 * 1024;
    /**
     * zip list of file
     *
     * @param resFileList file(dir) list
     * @param zipFile     output zip file
     * @throws IOException
     */
    public static void zipFiles(Collection<File> resFileList, File zipFile) throws IOException {
        ZipOutputStream zipout = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(zipFile), BUFFER_SIZE));
        for (File resFile : resFileList) {
            if (resFile.exists()) {
                zipFile(resFile, zipout, "");
            }
        }
        zipout.close();
    }

    private static void zipFile(File resFile, ZipOutputStream zipout, String rootpath) throws IOException {
        rootpath = rootpath + (rootpath.trim().length() == 0 ? "" : File.separator) + resFile.getName();
        if (resFile.isDirectory()) {
            File[] fileList = resFile.listFiles();
            for (File file : fileList) {
                zipFile(file, zipout, rootpath);
            }
        } else {
            final byte[] fileContents = readContents(resFile);
            //zip to android file separator
            if (rootpath.contains("\\")) {
                rootpath = rootpath.replace("\\", "/");
            }
            ZipEntry entry = new ZipEntry(rootpath);
            entry.setMethod(ZipEntry.DEFLATED);
            zipout.putNextEntry(entry);
            zipout.write(fileContents);
            zipout.flush();
            zipout.closeEntry();
        }
    }

    private static byte[] readContents(final File file) throws IOException {
        final ByteArrayOutputStream output = new ByteArrayOutputStream();
        final int bufferSize = BUFFER_SIZE;
        try {
            final FileInputStream in = new FileInputStream(file);
            final BufferedInputStream bIn = new BufferedInputStream(in);
            int length;
            byte[] buffer = new byte[bufferSize];
            byte[] bufferCopy;
            while ((length = bIn.read(buffer, 0, bufferSize)) != -1) {
                bufferCopy = new byte[length];
                System.arraycopy(buffer, 0, bufferCopy, 0, length);
                output.write(bufferCopy);
            }
            bIn.close();
        } finally {
            output.close();
        }
        return output.toByteArray();
    }



    public static void zipInputDir(File inputDir, File outputFile) throws IOException {
        File[] unzipFiles = inputDir.listFiles();
        List<File> collectFiles = new ArrayList<>();
        for (File f : unzipFiles) {
            collectFiles.add(f);
        }

        zipFiles(collectFiles, outputFile);
    }

}
