package com.crazymakercircle.ylem.iodemo.NioDiscard;


import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class DiscardClient {

    public static void echo(String ip, int port) {
        try {
            // 1.
            SocketChannel socketChannel = SocketChannel.open();
            socketChannel.configureBlocking(false);
            socketChannel.connect(new InetSocketAddress(ip, port));

            // 2. 等待连接完成
            while (! socketChannel.finishConnect() ) {
                System.out.println("正在连接服务器...");
            }

            // 3. 创建缓冲区
            ByteBuffer buffer = ByteBuffer.allocate(16);

            // 4. 读取数据并写入
            Scanner inScanner = new Scanner(System.in);
            while (inScanner.hasNext()) {
                String str = inScanner.next();

                // 数据写入缓冲区
                // 问题：数据缓冲区最大是16byte，所以如果超过16个byte，那么就会抛出异常
                buffer.put(str.getBytes(StandardCharsets.UTF_8));

                // 数据写入通道
                buffer.flip();
                socketChannel.write(buffer);

                // 清除缓冲区
                buffer.clear();
            }

            // 关闭通道
            socketChannel.shutdownOutput();
            socketChannel.close();
        } catch(IOException e) {

        } finally {

        }
    }


    public static void echoPro(String ip, int port) {
        try {
            SocketChannel socketChannel = SocketChannel.open();
            socketChannel.configureBlocking(false);
            socketChannel.connect(new InetSocketAddress(ip, port));

            while (! socketChannel.finishConnect() )  {
                System.out.println("正在连接服务器...");
            }

            ByteBuffer buffer = ByteBuffer.allocate(16);

            Scanner in = new Scanner(System.in);

            while (in.hasNext()) {
                String str = in.next();

                sendToServer(socketChannel, str, buffer);
            }

            // 关闭
            socketChannel.shutdownOutput();
            socketChannel.close();
        } catch(IOException e) {

        }
    }

    public static void sendToServer(SocketChannel socketChannel, String str, ByteBuffer buffer)
            throws IOException {
        int currentIndex = 0;
        int totalLen = 0;

        byte[] strByte = str.getBytes(StandardCharsets.UTF_8);
        int needSendLen = strByte.length;

        while (totalLen < needSendLen) {
            buffer.clear();

            int restLen = needSendLen - currentIndex;
            // 这里的length，既不能超过剩余未发送的byte长度；也不能超过buffer的容量
            // 所以取小值
            buffer.put(strByte, currentIndex,
                    (Math.min(restLen, buffer.capacity())));

            buffer.flip();
            totalLen += buffer.limit();

            currentIndex += buffer.limit();

            socketChannel.write(buffer);
        }
    }

    public static void main(String[] args) {
        // echo("127.0.0.1", 8299);
        echoPro("127.0.0.1", 8299);
    }
}
