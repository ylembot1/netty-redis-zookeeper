package com.crazymakercircle.iodemo.fileDemos;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class MyFileNIOCopyDemo {
    public static void main(String[] args) {
        String srcPath = "";
        String destPath = "";

        copy(srcPath, destPath);
    }

    public static void copy(String srcPath, String destPath) {
        File srcFile = new File(srcPath);
        File destFile = new File(destPath);

        try {
            if (!destFile.exists()) {
                destFile.createNewFile();
            }
            
            FileInputStream fis = new FileInputStream(srcFile);
            FileOutputStream fos = new FileOutputStream(destFile);
            FileChannel inChannel = fis.getChannel();
            FileChannel outChannel = fos.getChannel();

            ByteBuffer buf = ByteBuffer.allocate(1024);
            int len = -1;
            while ((len = inChannel.read(buf)) != -1) {
                System.out.println("read len: " + len);

                buf.flip();
                int outLen = -1;
                while ((outLen = outChannel.write(buf)) != 0) {
                    System.out.println("write len: " + outLen);
                }
                buf.clear();
            }
            outChannel.force(true);
        } catch(IOException e) {
        }
    }
}
