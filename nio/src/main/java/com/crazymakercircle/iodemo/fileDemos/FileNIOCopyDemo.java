package com.crazymakercircle.iodemo.fileDemos;

import com.oracle.tools.packager.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class FileNIOCopyDemo {

    public static void main(String[] args) {
        nioCopyResourceFile();
    }

    private static void nioCopyResourceFile() {
        String srcPath = "./tt.txt";

        String destPath = "./abc.txt";

        nioCopyFile(srcPath, destPath);
    }

    public static void nioCopyFile(String srcPath, String destPath) {
        File srcFile = new File(srcPath);
        File destFile = new File(destPath);

        try {
            if (!destFile.exists()) {
                destFile.createNewFile();
            }
            long startTime = System.currentTimeMillis();
            FileInputStream fis = null;
            FileOutputStream fos = null;
            FileChannel inChannel = null;
            FileChannel outChannel = null;

            try {
                fis = new FileInputStream(srcFile);
                fos = new FileOutputStream(destFile);
                inChannel = fis.getChannel();
                outChannel = fos.getChannel();
                int length = -1;
                ByteBuffer buf = ByteBuffer.allocate(1024);
                while ((length = inChannel.read(buf)) != -1) {
                    buf.flip();
                    int outLength = 0;

                    while ((outLength = outChannel.write(buf)) != 0) {
                        System.out.println("写入的字节数: " + outLength);
                    }
                    buf.clear();
                }
                outChannel.force(true);
            } finally {

            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
