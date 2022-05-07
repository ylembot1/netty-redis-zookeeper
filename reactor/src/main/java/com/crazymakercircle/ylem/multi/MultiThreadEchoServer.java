package com.crazymakercircle.ylem.multi;


import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

public class MultiThreadEchoServer {
    private Selector[] selectors = new Selector[2];
    private ServerSocketChannel serverSocketChannel = null;
    private SubReactor[] subReactors = null;

    MultiThreadEchoServer(int port) throws IOException {
        selectors[0] = Selector.open();
        selectors[1] = Selector.open();

        serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.bind(new InetSocketAddress(port));
        serverSocketChannel.configureBlocking(false);

        SelectionKey key = serverSocketChannel.register(selectors[0], 0);
        key.attach(new AcceptEventHandler());
        key.interestOps(SelectionKey.OP_ACCEPT);

        SubReactor subReactor1 = new SubReactor(selectors[0]);
        SubReactor subReactor2 = new SubReactor(selectors[1]);
        subReactors = new SubReactor[] {subReactor1, subReactor2};
    }

    private void startServer() {
        new Thread(subReactors[0]).start();
        new Thread(subReactors[1]).start();
    }

    class AcceptEventHandler implements Runnable {
        @Override
        public void run() {
            try {
                if (serverSocketChannel == null || selectors[1] == null) {
                    return;
                }

                SocketChannel socketChannel = serverSocketChannel.accept();

                if (socketChannel != null) {
                    System.out.println("has one connection: " + socketChannel.getRemoteAddress());
                    new IOEventHandler(selectors[1], socketChannel);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    class SubReactor implements Runnable {

        private Selector selector = null;

        SubReactor(Selector selector) {
            this.selector = selector;
        }

        @Override
        public void run() {
            try {
                while (!Thread.interrupted()) {
                    selector.select();
                    Set<SelectionKey> selectionKeys = selector.selectedKeys();
                    Iterator<SelectionKey> iter = selectionKeys.iterator();
                    while (iter.hasNext()) {
                        SelectionKey key = iter.next();
                        dispatch(key);
                    }
                    selectionKeys.clear();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void dispatch(SelectionKey key) {
            Runnable handler = (Runnable) key.attachment();

            if (handler == null) {
                System.out.println("error: no handler");
                return;
            }
            handler.run();
        }
    }

    public static void main(String[] args) throws IOException {
        new MultiThreadEchoServer(8299).startServer();
    }
}



