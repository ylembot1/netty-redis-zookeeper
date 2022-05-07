package com.crazymakercircle.ylem.multi;


import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class IOEventHandler implements Runnable {

    private Selector selector = null;
    private SocketChannel socketChannel = null;
    private SelectionKey key = null;

    private ByteBuffer buffer = null;
    private ExecutorService pool = null;

    private final int RECEIVE = 0, SENDING = -1;
    private int state = RECEIVE;

    private String msg = "";

    IOEventHandler(Selector selector, SocketChannel socketChannel) {
        this.selector = selector;
        this.socketChannel = socketChannel;

        connectionInit();
        otherInit();
    }

    private void otherInit() {
        buffer = ByteBuffer.allocate(16);
        pool = Executors.newFixedThreadPool(4);
    }

    private void connectionInit() {
        if (selector == null || socketChannel == null) {
            return;
        }

        try {
            System.out.println("connectionInit begin..");
            socketChannel.configureBlocking(false);
            key = socketChannel.register(selector, 0);
            key.attach(this);
            key.interestOps(SelectionKey.OP_READ);

            selector.wakeup();
            System.out.println("connectionInit end..");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        pool.execute(new AsyncTask());
    }

    class AsyncTask implements Runnable {

        @Override
        public void run() {
            IOEventHandler.this.asyncRun();
        }
    }

    private void asyncRun() {
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

//                    // 模拟阻塞
//                    Thread.sleep(10000);
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
        } catch(IOException e) {
            e.printStackTrace();
        }
    }
}
