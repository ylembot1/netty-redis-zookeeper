package com.crazymakercircle.iodemo.NioDiscard;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Scanner;

public class NioDiscardClient {
    public static void main(String[] args) throws IOException {
        InetSocketAddress serverAddress = new InetSocketAddress("127.0.0.1", 8080);
        SocketChannel socketChannel = SocketChannel.open(serverAddress);
        // socketChannel.bind(serverAddress);   // 客户端当然不能用bind！！
        socketChannel.configureBlocking(false);

        while (!socketChannel.finishConnect()) {

        }

        ByteBuffer buffer = ByteBuffer.allocate(1024);
        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNext()) {
            String msg = scanner.next();
            buffer.put(msg.getBytes());

            buffer.flip();
            socketChannel.write(buffer);

            buffer.clear();
        }

        scanner.close();
        socketChannel.shutdownOutput();
        socketChannel.close();
    }
}
