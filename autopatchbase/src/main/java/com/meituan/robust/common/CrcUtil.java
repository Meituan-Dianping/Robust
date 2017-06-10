package com.meituan.robust.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.CRC32;

/**
 * Created by hedingxu on 17/6/5.
 */

public class CrcUtil {
    private CrcUtil(){

    }
    public static long computeFileCrc32(File file) throws IOException {
        InputStream inputStream = new FileInputStream(file);
        CRC32 crc = new CRC32();
        int index;
        while ((index = inputStream.read()) != -1) {
            crc.update(index);
        }
        return crc.getValue();
    }
}
