package com.crazymakercircle.ReactorModel;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class MultiThreadEchoServerReactor {
    ServerSocketChannel serverSocket;
    AtomicInteger next = new AtomicInteger(0);

    // 选择器集合，引入多个选择器
    Selector[] selectors = new Selector[2];
    SubReactor[] subReactors = null;
    MultiThreadEchoServerReactor() throws IOException {
        // 初始化多个选择器
        selectors[0] = Selector.open();
        selectors[1] = Selector.open();

        serverSocket = ServerSocketChannel.open();
        InetSocketAddress address = new InetSocketAddress("127.0.0.1", 8299);
        serverSocket.bind(address);
        // 非阻塞
        serverSocket.configureBlocking(false);
        // 第一个选择器，负责监控新连接事件
        SelectionKey sk = serverSocket.register(selectors[0], SelectionKey.OP_ACCEPT);
        // 绑定Handler
        sk.attach(new AcceptorHandler());
        // 第一个子反应器，一个子反应器负责一个选择器
        SubReactor subReactor1 = new SubReactor(selectors[0]);
        // 第二个子反应器，一个子反应器负责一个选择器
        SubReactor subReactor2 = new SubReactor(selectors[1]);

        subReactors = new SubReactor[] {subReactor1, subReactor2};
    }

    private void startService() {
        new Thread(subReactors[0]).start();
        new Thread(subReactors[1]).start();
    }

    // 子反应器
    class SubReactor implements Runnable {
        // 每个选择器负责一个选择器的查询和选择
        final Selector selector;

        public SubReactor(Selector selector) {
            this.selector = selector;
        }

        @Override
        public void run() {
            try {
                while (!Thread.interrupted()) {
                    selector.select();
                    Set<SelectionKey> selectionKeySet = selector.selectedKeys();
                    Iterator<SelectionKey> it = selectionKeySet.iterator();
                    while (it.hasNext()) {
                        SelectionKey selectionKey =  it.next();
                        dispatch(selectionKey);
                    }
                    selectionKeySet.clear();
                }
            } catch(IOException e) {
                e.printStackTrace();
            }
        }

        void dispatch(SelectionKey sk) {
            Runnable handler = (Runnable) sk.attachment();
            if (handler != null) {
                handler.run();
            }
        }
    }

    class AcceptorHandler implements Runnable {

        @Override
        public void run() {
            try {
                SocketChannel socketChannel = serverSocket.accept();
                if (socketChannel != null) {
                    new MultiThreadEchoHandler(selectors[next.get()], socketChannel);
                }
            } catch(IOException e) {
                e.printStackTrace();
            }
            if (next.incrementAndGet() == selectors.length) {
                next.set(0);
            }
        }
    }

    public static void main(String[] args) throws IOException {
        MultiThreadEchoServerReactor server = new MultiThreadEchoServerReactor();
        server.startService();
    }
}
