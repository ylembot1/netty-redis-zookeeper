package com.crazymakercircle.ReactorModel;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Set;

public class EchoClient {

    public void start() throws IOException {

        InetSocketAddress address = new InetSocketAddress("127.0.0.1", 8299);
        SocketChannel channel = SocketChannel.open(address);
        channel.configureBlocking(false);
        while(!channel.finishConnect()) {
            // 等待连接完成
        }

        Processer processer = new Processer(channel);
        new Thread(processer).start();
    }

    static class Processer implements Runnable {

        final Selector selector;
        final SocketChannel socketChannel;

        Processer(SocketChannel channel) throws IOException {
            socketChannel = channel;
            selector = Selector.open();
            channel.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
        }


        @Override
        public void run() {
            try {
                while (!Thread.interrupted()) {
                    selector.select();
                    Set<SelectionKey> selectionKeys = selector.selectedKeys();
                    Iterator<SelectionKey> it = selectionKeys.iterator();
                    while (it.hasNext()) {
                        SelectionKey sk =  it.next();
                        if (sk.isWritable()) {
                            ByteBuffer buffer = ByteBuffer.allocate(1024);
                            Scanner scanner = new Scanner(System.in);
                            while (scanner.hasNext()) {
                                String str = scanner.next();
                                SocketChannel channel = (SocketChannel) sk.channel();
                                buffer.put(str.getBytes());
                                buffer.flip();
                                channel.write(buffer);
                                buffer.clear();
                            }
                        }
                        if (sk.isReadable()) {
                            ByteBuffer buffer = ByteBuffer.allocate(1024);
                            SocketChannel channel = (SocketChannel) sk.channel();
                            int length = 0;
                            while ((length = channel.read(buffer)) > 0) {
                                buffer.flip();
                                System.out.println("server echo: " +
                                        new String(buffer.array(), 0, length));
                                buffer.clear();
                            }
                        }
                    }
                    selectionKeys.clear();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) throws Exception {
        new EchoClient().start();
    }
}
