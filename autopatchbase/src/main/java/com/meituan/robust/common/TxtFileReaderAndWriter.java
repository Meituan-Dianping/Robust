package com.meituan.robust.common;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;


/**
 * Created by hedingxu on 17/6/8.
 */
public final class TxtFileReaderAndWriter {

    private static final String BACKSLASH = "\\";
    private static final String SLASH = "/";


    private TxtFileReaderAndWriter() {
        // util class.
    }

    public static String readFileAsString(final String filename) {
        return new String(readFileAsBytes(filename));
    }

    public static String readFileAsString(final File file) {
        return new String(readFileAsBytes(file));
    }

    public static byte[] readFileAsBytes(final String filename) {
        return readFileAsBytes(new File(filename));
    }

    public static byte[] readFileAsBytes(final File file) {
        FileInputStream in;
        try {
            in = new FileInputStream(file);
        } catch (FileNotFoundException ex) {
            return null;
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        copyStream(in, out);
        return out.toByteArray();
    }

    public static void writeFile(final String filename, final String str) {
        writeFile(filename, str.getBytes());
    }

    public static void writeFile(final File file, final String str) {
        writeFile(file, str.getBytes());
    }

    public static void writeFile(final String filename, final byte[] bytes) {
        writeFile(new File(filename), bytes);
    }

    public static void writeFile(final File file, final byte[] bytes) {
        writeFile(file, new ByteArrayInputStream(bytes));
    }

    public static void writeFile(final File file, final InputStream in) {
        copyStream(in, createFileOutputStream(file));
    }

    private static FileOutputStream createFileOutputStream(File file) {
        if (!file.getParentFile().exists()) {
            boolean success = file.getParentFile().mkdirs();
            if (!success) {
                return null;
            }
        }
        try {
            return new FileOutputStream(file);
        } catch (FileNotFoundException ex) {
            return null;
        }
    }

    public static void copyStream(InputStream in, OutputStream out) {
        try {
            int b = in.read();
            while (b > -1) {
                out.write(b);
                b = in.read();
            }
            in.close();
            out.close();
        } catch (IOException ex) {
            return;
        }
    }

    public static void copyFile(File src, File target) {
        try {
            copyStream(new FileInputStream(src), createFileOutputStream(target));
        } catch (FileNotFoundException ex) {
            return ;
        }
    }

    public static boolean exists(String filename) {
        return (new File(filename)).exists();
    }

    public static String getAbsolutePath(String filename) {
        File file = new File(filename);
        return replaceBackslash(file.getAbsolutePath());
    }

    public static String getCanonicalPath(String filename) {
        File file = new File(filename);
        try {
            return replaceBackslash(file.getCanonicalPath());
        } catch (IOException ex) {
            return null;
        }
    }

    public static boolean isAbsolute(String filename) {
        File file = new File(filename);
        return file.isAbsolute();
    }

    public static String getParent(String filename) {
        File file = new File(filename);
        return replaceBackslash(file.getParent());
    }

    private static String replaceBackslash(String str) {
        return str.replace(BACKSLASH, SLASH);
    }

    /**
     * Convenience method for testing.
     *
     * @param content Piece of text to be scanned.
     * @param index1  Index to scan from (inclusive).
     * @param index2  Index to to scan unto (exclusive). If < 0 it is ignored.
     * @param strings String that must be contained.
     * @return Index of last string of strings.
     * @throws IllegalArgumentException if content does not contain any of the strings in the indicated substring.
     */
    public static int assertPresent(String content, int index1, int index2, String... strings) {
        validateArgs(index1, strings);
        int index = -1;
        for (String str : strings) {
            index = content.indexOf(str, index1);
            assertTrue("Expected: index1 <= index for " + str + ", index1=" + index1 + ", index=" + index,
                    index1 <= index);
            if (index2 >= 0) {
                assertTrue("Expected: index < index2 for " + str + ", index=" + index + ", index2=" + index2,
                        index < index2);
            }
        }
        return index;
    }

    /**
     * Convenience method for testing. Opposite of {@link #assertPresent(String, int, int, String...)}.
     *
     * @param content Idem as at #assertPresent(String, int, int, String...).
     * @param index1  Idem.
     * @param index2  Index to to scan unto (exclusive).
     * @param strings String that must not be contained.
     * @throws IllegalArgumentException if content does contain any of the strings in the indicated substring.
     */
    public static void assertNotPresent(String content, int index1, int index2, String... strings) {
        validateArgs(index1, strings);
        for (String str : strings) {
            int index = content.indexOf(str, index1);
            assertTrue("Expected: index < 0 || index >= index2 for " + str + ", index=" + index
                            + ", index1=" + index1 + ", index2=" + index2,
                    index < 0 || index >= index2);
        }
    }

    private static void validateArgs(int index1, String[] strings) {
        assertTrue("Expected: index1 >= 0, index1=" + index1, index1 >= 0);
        assertTrue("Expected: strings.length > 0", strings.length > 0);
    }

    private static void assertTrue(String msg, boolean condition) {
        if (!condition) {
            throw new IllegalArgumentException(msg);
        }
    }
}