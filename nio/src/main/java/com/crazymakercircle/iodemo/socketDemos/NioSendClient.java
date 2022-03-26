package com.crazymakercircle.iodemo.socketDemos;

import java.io.File;
import java.io.FileInputStream;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class NioSendClient {

    private Charset charset = Charset.forName("UTF-8");

    public void sendFile() throws Exception {
        try {
            String srcPath = "xxx";
            File file = new File(srcPath);
            if (!file.exists()) {
                return;
            }
            FileChannel fileChannel = new FileInputStream(file).getChannel();
            SocketChannel socketChannel = SocketChannel.open();
            socketChannel.socket().connect(new InetSocketAddress("127.0.0.1", 8299));

            socketChannel.configureBlocking(false);
            while (!socketChannel.finishConnect()) {}

            // 发送文件名称
            ByteBuffer fileNameBuffer = ByteBuffer.allocate(1024);
            fileNameBuffer.put("xxx".getBytes());
            fileNameBuffer.flip();
            socketChannel.write(fileNameBuffer);

            // 文件长度
            ByteBuffer buffer = ByteBuffer.allocate(1024);
            buffer.put(String.valueOf(file.length()).getBytes());
            buffer.flip();
            socketChannel.write(buffer);
            buffer.clear();

            // 发送文件内容
            int length = 0;
            int progress = 0;
            while ((length = fileChannel.read(buffer)) > 0) {
                buffer.flip();
                socketChannel.write(buffer);
                buffer.clear();
                progress += length;
            }
            if (length == -1) {
                fileChannel.close();

                // 向对端发送结束标志
                socketChannel.shutdownOutput();
                socketChannel.close();
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws Exception {
        NioSendClient client = new NioSendClient();
        client.sendFile();
    }
}
