package com.crazymakercircle.ReactorModel;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

public class EchoServerReactor implements Runnable {
    Selector selector;
    ServerSocketChannel serverSocket;
    EchoServerReactor() throws IOException {
        selector = Selector.open();
        serverSocket = ServerSocketChannel.open();

        InetSocketAddress address = new InetSocketAddress("127.0.0.1", 8299);
        serverSocket.bind(address);
        serverSocket.configureBlocking(false);

        SelectionKey sk = serverSocket.register(selector, SelectionKey.OP_ACCEPT);
        sk.attach(new AcceptorHandler());
    }

    public void run() {
        try {
            while (!Thread.interrupted()) {
                selector.select();
                Set<SelectionKey> selected = selector.selectedKeys();
                Iterator<SelectionKey> it = selected.iterator();

                while (it.hasNext()) {
                    SelectionKey sk =  it.next();
                    dispatch(sk);
                }
                selected.clear();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    void dispatch(SelectionKey sk) {
        Runnable handler = (Runnable) sk.attachment();
        if (handler != null) {
            handler.run();
        }
    }

    class AcceptorHandler implements Runnable {
        public void run() {
            try {
                SocketChannel channel = serverSocket.accept();
                if (channel != null) {
                    new EchoHandler(selector, channel);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    class EchoHandler implements Runnable {
        final SocketChannel channel;
        final SelectionKey sk;
        final ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
        static final int RECEIVING = 0, SENDING = 1;
        int state = RECEIVING;

        EchoHandler(Selector selector, SocketChannel c) throws IOException {
            channel = c;
            c.configureBlocking(false);
            sk = channel.register(selector, 0);
            sk.attach(this);
            sk.interestOps(SelectionKey.OP_READ);
            selector.wakeup();
        }

        public void run() {
            try {
                if (state == SENDING) {
                    channel.write(byteBuffer);
                    byteBuffer.clear();
                    sk.interestOps(SelectionKey.OP_READ);
                    state = RECEIVING;
                } else if(state == RECEIVING) {
                    int length = 0;
                    while ((length = channel.read(byteBuffer)) > 0) {
                        System.out.println(new String(byteBuffer.array(), 0, length));
                    }
                    byteBuffer.flip();
                    sk.interestOps(SelectionKey.OP_WRITE);
                    state = SENDING;
                }
            } catch(IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) throws IOException {
        new Thread(new EchoServerReactor()).start();
    }
}
