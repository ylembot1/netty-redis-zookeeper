package com.crazymakercircle.ylem.single;


import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Set;

/**
 * reactor: 事件分发器
 */
public class EchoServerReactor implements Runnable {
    private Selector selector = null;
    private ServerSocketChannel serverSocketChannel = null;

    EchoServerReactor() throws IOException {
        selector = Selector.open();
        serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.bind(new InetSocketAddress(8299));

        SelectionKey key = serverSocketChannel.register(selector, 0);
        key.attach(new AcceptHandler(selector, serverSocketChannel));
        key.interestOps(SelectionKey.OP_ACCEPT);
    }


    @Override
    public void run() {
        try {
            // 无限循环
            while (!Thread.interrupted()) {
                while (selector.select() > 0) {
                    Set<SelectionKey> selectionKeys = selector.selectedKeys();
                    Iterator<SelectionKey> iter = selectionKeys.iterator();
                    while (iter.hasNext()) {
                        SelectionKey key = iter.next();
                        dispatch(key);
                    }
                    selectionKeys.clear();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void dispatch(SelectionKey key) {
        Runnable handler = (Runnable) key.attachment();

        if (handler != null) {
            handler.run();
        }
    }
}

/**
 * 连接处理handler
 */
class AcceptHandler implements Runnable {

    private ServerSocketChannel serverSocketChannel;
    private Selector selector;

    AcceptHandler(Selector selector, ServerSocketChannel serverSocketChannel) {
        this.selector = selector;
        this.serverSocketChannel = serverSocketChannel;
    }

    @Override
    public void run() {
        try {
            if (selector == null || serverSocketChannel == null) {
                return;
            }

            SocketChannel socketChannel = serverSocketChannel.accept();

            if (socketChannel != null) {
                System.out.println("has one client connected: " + socketChannel.getRemoteAddress());
                new EchoHandler(selector, socketChannel);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

class EchoHandler implements Runnable {
    private Selector selector = null;
    private SocketChannel socketChannel = null;
    SelectionKey key = null;

    private final int RECEIVE = 0, SENDING = 1;
    private int state = RECEIVE;

    ByteBuffer buffer = null;
    String msg = "";

    EchoHandler(Selector selector, SocketChannel socketChannel) throws IOException {
        this.selector = selector;
        this.socketChannel = socketChannel;

        connectionInit();
        otherInit();
    }

    private void connectionInit() throws IOException {
        socketChannel.configureBlocking(false);
        key = socketChannel.register(selector, 0);

        key.attach(this);
        key.interestOps(SelectionKey.OP_READ);
        selector.wakeup();
    }

    private void otherInit() {
        this.buffer = ByteBuffer.allocate(16);
    }

    @Override
    public void run() {
        try {
            if (state == RECEIVE) {
                int currentLength = 0;
                int totalLength = 0;

                buffer.clear();
                while ((currentLength = socketChannel.read(buffer)) > 0) {
                    String str = new String(buffer.array(), 0, currentLength);
                    msg += str;

                    System.out.println("slice: " + str);

                    totalLength += currentLength;

                    buffer.clear();
                }
                System.out.println("whole msg: " + msg);
                System.out.println("length: " + totalLength);

                state = SENDING;
                key.interestOps(SelectionKey.OP_WRITE);

                // 模拟阻塞
                Thread.sleep(10000);
            } else if (state == SENDING) {
                int currentIndex = 0;
                int totalLength = 0;

                byte[] sendByte = msg.getBytes(StandardCharsets.UTF_8);
                int sendLength = sendByte.length;

                while (totalLength < sendLength) {
                    buffer.clear();

                    int restLength = sendByte.length - currentIndex;
                    int readyToSendLength = Math.min(restLength, buffer.capacity());
                    buffer.put(sendByte, currentIndex, readyToSendLength);

                    buffer.flip();
                    socketChannel.write(buffer);

                    totalLength += readyToSendLength;
                    currentIndex += readyToSendLength;
                }
                System.out.println("send msg total length: " + totalLength);

                msg = "";
                state = RECEIVE;
                key.interestOps(SelectionKey.OP_READ);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {
        new Thread(new EchoServerReactor()).start();
    }
}
