package com.crazymakercircle.ylem.iodemo.fileDemos;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class FileCopy {

    public static boolean fileCopy(String src, String dst) {
        if (src == null || src.isEmpty()) {
            return false;
        }
        if (dst == null || dst.isEmpty()) {
            return false;
        }

        try {
            File srcFile = new File(src);
            if (!srcFile.exists()) {
                return false;
            }

            File dstFile = new File(dst);
            if (!dstFile.exists()) {
                dstFile.createNewFile();
            }

            FileChannel inChannel = new FileInputStream(srcFile).getChannel();
            FileChannel outChannel = new FileOutputStream(dstFile).getChannel();

            int totalLength = 0;
            int currentLength = 0;
            ByteBuffer buffer = ByteBuffer.allocate(128);

            int i = 0;
            while ((currentLength = inChannel.read(buffer)) != -1) {
                i++;
                System.out.println("第" + i + "次：" + currentLength);

                totalLength += currentLength;
                buffer.flip();
                outChannel.write(buffer);
                buffer.clear();
            }
            System.out.println("复制完成：" + totalLength);

        } catch (IOException e) {
            return false;
        }

        return true;
    }

    public static void main(String[] args) {
        fileCopy("~/Desktop/书籍/第一本Docker书 PDF电子书下载 带书签目录 完整版.pdf",
                "~/Desktop/第一本Docker书 PDF电子书下载 带书签目录 完整版.pdf");
    }
}
