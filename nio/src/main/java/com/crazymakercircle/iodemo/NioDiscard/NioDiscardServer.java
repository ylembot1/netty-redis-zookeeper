package com.crazymakercircle.iodemo.NioDiscard;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

public class NioDiscardServer {
    public static void startServer() throws IOException {
        // 1. 获取选择器
        Selector selector = Selector.open();
        // 2. 获取通道
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        // 3. 设置为非阻塞
        serverSocketChannel.configureBlocking(false);
        // 4. 绑定连接
        serverSocketChannel.bind(new InetSocketAddress("127.0.0.1", 8299));
        // 5. 绑定选择器
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        // 6. 轮询感兴趣的事件
        while (selector.select() > 0) {
            // 7. 获取选择键集合
            Iterator<SelectionKey> selectedKeys = selector.selectedKeys().iterator();

            while (selectedKeys.hasNext()) {
                // 8. 获取单个键，并处理
                SelectionKey selectionKey = selectedKeys.next();
                // 9. 判断key具体是什么事件
                if (selectionKey.isAcceptable()) {
                    SocketChannel socketChannel = serverSocketChannel.accept();
                    socketChannel.configureBlocking(false);

                    System.out.println("has one connected");

                    socketChannel.register(selector, SelectionKey.OP_READ);
                } else if (selectionKey.isReadable()) {
                    // 13. 若选择键是IO可读事件，读取数据
                    SocketChannel socketChannel = (SocketChannel) selectionKey.channel();

                    // 14.读取数据，然后丢弃
                    ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
                    int length = 0;
                    while ((length = socketChannel.read(byteBuffer)) > 0) {
                        byteBuffer.flip();
                        System.out.println(new String(byteBuffer.array(), 0, byteBuffer.limit(), "UTF-8"));

                        byteBuffer.clear();
                    }
                    // socketChannel.close();
                }
                selectedKeys.remove();
            }
        }
        // 16. 关闭连接
        serverSocketChannel.close();
    }

    public static void main(String[] args) throws Exception {
        startServer();
    }
}
