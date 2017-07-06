package com.meituan.robust.resource.diff.util;

import com.meituan.robust.tools.jbdiff.JBDiff;
import com.meituan.robust.tools.jbdiff.JBPatch;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by hedingxu on 17/3/26.
 */

public class DiffAndRecoverUtil {
    public static void diff(File oldFile, File newFile, File diffFile) throws IOException {
        JBDiff.bsdiff(oldFile, newFile, diffFile);
    }

    public static void recover(InputStream oldInputStream, InputStream diffInputStream, File newFile) throws IOException {
        if (oldInputStream == null) {
            throw new IOException("old input stream is null");
        }
        if (newFile == null) {
            throw new IOException("new file is null");
        }
        if (diffInputStream == null) {
            throw new IOException("diff input stream is null");
        }

        byte[] oldBytes = toByteArray(oldInputStream);
        byte[] diffBytes = toByteArray(diffInputStream);

        if (newFile.exists()) {
            newFile.delete();
        }

        byte[] newBytes = JBPatch.bspatch(oldBytes, oldBytes.length, diffBytes, diffBytes.length);

        OutputStream newOutputStream = new FileOutputStream(newFile);
        try {
            newOutputStream.write(newBytes);
        } catch (Throwable throwable) {

        } finally {
            newOutputStream.close();
        }
    }

    public static void recover(File baseFile, File diffFile, File newFile) {
        if (null == baseFile || null == diffFile || null == newFile) {
            return;
        }
        if (!baseFile.exists() || !diffFile.exists()) {
            return;
        }
        if (newFile.exists()) {
            newFile.delete();
        }

        try {
            InputStream inputStreamBase = new FileInputStream(baseFile);
            InputStream inputStreamDiff = new FileInputStream(diffFile);
            recover(inputStreamBase, inputStreamDiff, newFile);
            return;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

//    ======================================================================================================

    /**
     * Get the contents of an <code>InputStream</code> as a <code>byte[]</code>.
     * <p>
     * This method buffers the input internally, so there is no need to use a
     * <code>BufferedInputStream</code>.
     *
     * @param input the <code>InputStream</code> to read from
     * @return the requested byte array
     * @throws NullPointerException if the input is null
     * @throws IOException          if an I/O error occurs
     */
    private static byte[] toByteArray(InputStream input) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        copy(input, output);
        return output.toByteArray();
    }

    /**
     * Copy bytes from an <code>InputStream</code> to an
     * <code>OutputStream</code>.
     * <p>
     * This method buffers the input internally, so there is no need to use a
     * <code>BufferedInputStream</code>.
     * <p>
     * Large streams (over 2GB) will return a bytes copied value of
     * <code>-1</code> after the copy has completed since the correct
     * number of bytes cannot be returned as an int. For large streams
     * use the <code>copyLarge(InputStream, OutputStream)</code> method.
     *
     * @param input  the <code>InputStream</code> to read from
     * @param output the <code>OutputStream</code> to write to
     * @return the number of bytes copied, or -1 if &gt; Integer.MAX_VALUE
     * @throws NullPointerException if the input or output is null
     * @throws IOException          if an I/O error occurs
     */
    private static int copy(InputStream input, OutputStream output) throws IOException {
        long count = copyLarge(input, output);
        if (count > Integer.MAX_VALUE) {
            return -1;
        }
        return (int) count;
    }

    /**
     * The default buffer size ({@value}) to use for
     * {@link #copyLarge(InputStream, OutputStream)}
     */
    private static final int DEFAULT_BUFFER_SIZE = 1024 * 4;

    /**
     * Copy bytes from a large (over 2GB) <code>InputStream</code> to an
     * <code>OutputStream</code>.
     * <p>
     * This method buffers the input internally, so there is no need to use a
     * <code>BufferedInputStream</code>.
     * <p>
     * The buffer size is given by {@link #DEFAULT_BUFFER_SIZE}.
     *
     * @param input  the <code>InputStream</code> to read from
     * @param output the <code>OutputStream</code> to write to
     * @return the number of bytes copied
     * @throws NullPointerException if the input or output is null
     * @throws IOException          if an I/O error occurs
     */
    private static long copyLarge(InputStream input, OutputStream output)
            throws IOException {
        return copyLarge(input, output, new byte[DEFAULT_BUFFER_SIZE]);
    }

    private static final int EOF = -1;

    /**
     * Copy bytes from a large (over 2GB) <code>InputStream</code> to an
     * <code>OutputStream</code>.
     * <p>
     * This method uses the provided buffer, so there is no need to use a
     * <code>BufferedInputStream</code>.
     * <p>
     *
     * @param input  the <code>InputStream</code> to read from
     * @param output the <code>OutputStream</code> to write to
     * @param buffer the buffer to use for the copy
     * @return the number of bytes copied
     * @throws NullPointerException if the input or output is null
     * @throws IOException          if an I/O error occurs
     */
    private static long copyLarge(InputStream input, OutputStream output, byte[] buffer)
            throws IOException {
        long count = 0;
        int n = 0;
        while (EOF != (n = input.read(buffer))) {
            output.write(buffer, 0, n);
            count += n;
        }
        return count;
    }

}
