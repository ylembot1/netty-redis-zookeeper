package com.crazymakercircle.iodemo.socketDemos;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class NioReceiveServer {

    private Charset charset = Charset.forName("UTF-8");

    // 服务器保存的客户端对象，对应一个客户端文件
    static class Client {
        // 文件名称
        String fileName;
        // 文件长度
        long fileLength;
        // 开始传输的时间
        long startTime;
        // 客户端的地址
        InetSocketAddress remoteAddress;
        // 输出的文件通道
        FileChannel outChannel;
    }

    private ByteBuffer buffer = ByteBuffer.allocate(1024);

    // 使用map保存每一文件传输，当OP_READ时，找到通道对应的对象
    Map<SelectableChannel, Client> clientMap = new HashMap<SelectableChannel, Client>();

    public void startServer() throws Exception {
        // 1. 获取选择器
        Selector selector = Selector.open();
        // 2. 获取通道
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        // 3. 设置通道为非阻塞
        serverSocketChannel.configureBlocking(false);
        // 4. 绑定连接
        serverSocketChannel.bind(new InetSocketAddress("127.0.0.1", 8299));
        // 5. 通道注册选择器
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

        System.out.println("server start!!");

        // 6. 就绪
        while (selector.select() > 0) {
            // 7. 获取key的集合
            Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();

            while (iterator.hasNext()) {
                // 8. 获取单个key，并处理
                SelectionKey key = iterator.next();

                // 判断key的类型
                if (key.isAcceptable()) {
                    ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
                    SocketChannel socketChannel = serverChannel.accept();
                    if (socketChannel == null) {
                        continue;
                    }
                    // 9. 客户端连接，设置为非阻塞
                    socketChannel.configureBlocking(false);
                    // 10. 注册到选择器
                    socketChannel.register(selector, SelectionKey.OP_READ);
                    // 11. 为每一条传输通道，创建一个client，保存起来，供后续传输使用
                    Client client = new Client();
                    client.remoteAddress = (InetSocketAddress) socketChannel.getRemoteAddress();
                    clientMap.put(socketChannel, client);

                    System.out.println("连接建立成功，客户端地址： " + socketChannel.getRemoteAddress().toString());
                } else if (key.isReadable()) {
                    processData(key);
                }
                iterator.remove();
            }
        }
    }

    private void processData(SelectionKey key) throws Exception {
        Client client = clientMap.get(key.channel());
        SocketChannel socketChannel = (SocketChannel) key.channel();
        int num = 0;
        try {
            buffer.clear();
            while ((num = socketChannel.read(buffer)) > 0) {
                buffer.flip();
                if (null == client.fileName) {
                    // 首先发送文件名称
                    String fileName = charset.decode(buffer).toString();
                    System.out.println(fileName);
                    File destFile = new File(fileName);
                    if (!destFile.exists()) {
                        destFile.createNewFile();
                    }
                    FileChannel fileChannel = new FileOutputStream(destFile).getChannel();
                    client.outChannel = fileChannel;
                } else if (0 == client.fileLength) {
                    long fileLength = Long.valueOf(new String(buffer.array(), 0, buffer.limit()));
                    client.fileLength = fileLength;
                    System.out.println("文件长度： " + fileLength);
                    client.startTime = System.currentTimeMillis();
                    System.out.println("传输开始：");
                } else {
                    client.outChannel.write(buffer);
                }
                buffer.clear();
            }
            key.cancel();
        } catch (IOException e) {
            key.cancel();
            e.printStackTrace();
            return;
        }

        if (num == -1) {
            client.outChannel.close();
            System.out.println("传输完毕, 时长： " + (System.currentTimeMillis() - client.startTime));
            key.cancel();
        }
    }

    public static void main(String[] args) throws Exception {
        new NioReceiveServer().startServer();
    }
}
