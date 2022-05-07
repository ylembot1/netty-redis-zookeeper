package com.crazymakercircle.ylem.iodemo.NioDiscard;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

public class DiscardServer {

    private static ByteBuffer buffer = ByteBuffer.allocate(10);

    public static void echo(int port) {

        try {
            // 1.
            Selector selector = Selector.open();

            // 2.
            ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.configureBlocking(false);
            serverSocketChannel.bind(new InetSocketAddress(port));

            // 3.
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

            // 4.
            while (selector.select() > 0) {
                Set<SelectionKey> selectionKeySet = selector.selectedKeys();
                Iterator<SelectionKey> iter = selectionKeySet.iterator();
                while (iter.hasNext()) {
                    SelectionKey key = iter.next();
                    if (key.isAcceptable()) {
                        System.out.println("acceptable");
                        acceptEventHandle(selector, key);
                    } else if (key.isReadable()) {
                        System.out.println("readable");
                        readEventHandle(key);
                    }
                    iter.remove();
                }
            }
        } catch (IOException e) {

        }
    }

    public static void acceptEventHandle(Selector selector, SelectionKey key) throws IOException {
        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
        SocketChannel socketChannel = serverSocketChannel.accept();

        System.out.println("remote address" + socketChannel.getRemoteAddress());

        socketChannel.configureBlocking(false);
        socketChannel.register(selector, SelectionKey.OP_READ);
    }

    public static void readEventHandle(SelectionKey key) throws IOException {
        SocketChannel socketChannel = (SocketChannel) key.channel();

        int currentLen = 0;
        int totalLen = 0;
        // 注意：> 0 而不是 != -1
        while ((currentLen = socketChannel.read(buffer)) > 0) {
            totalLen += currentLen;

            buffer.flip();
            // 注意：这里只能截取limit这么多
            String str = new String(buffer.array(), 0, buffer.limit());
            System.out.println("str: " + str);

            buffer.clear();
        }
        System.out.println("total len: " + totalLen);
    }

    public static void main(String[] args) {
        echo(8299);
    }
}
