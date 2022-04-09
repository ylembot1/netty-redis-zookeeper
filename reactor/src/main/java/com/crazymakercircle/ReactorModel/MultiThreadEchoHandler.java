package com.crazymakercircle.ReactorModel;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MultiThreadEchoHandler implements Runnable {
    final SocketChannel socketChannel;
    final SelectionKey sk;
    final ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
    static final int RECEIVING = 0, SENDING = 1;
    int state = RECEIVING;

    // 引入线程池
    static ExecutorService pool = Executors.newFixedThreadPool(4);

    MultiThreadEchoHandler(Selector selector, SocketChannel channel) throws IOException {
        socketChannel = channel;
        channel.configureBlocking(false);
        // 取得选择键，在设置感兴趣的IO事件
        sk = channel.register(selector, 0);
        sk.attach(this);
        // 向sk选择键注册Read就绪事件
        sk.interestOps(SelectionKey.OP_READ);
        selector.wakeup();
    }

    @Override
    public void run() {
        // 异步任务，在独立的线程池中执行
        pool.execute(new AsyncTask());
    }

    // 业务处理逻辑，不在反应器中执行
    public synchronized void asyncRun() {
        try {
            if (state == SENDING) {
                // 写入通道
                socketChannel.write(byteBuffer);
                // 写完之后，准备开始从通道读，byteBuffer切换到写入模式
                byteBuffer.clear();
                // 写完之后，注册read就绪事件
                sk.interestOps(SelectionKey.OP_READ);
                state = RECEIVING;
            } else if (state == RECEIVING) {
                // 从通道读
                int length = 0;
                while ((length = socketChannel.read(byteBuffer)) > 0) {
                    System.out.println(new String(byteBuffer.array(), 0, length));
                }
                // 读取完了之后，准备开始写入通道，byteBuffer切换到读取模式;
                byteBuffer.flip();
                sk.interestOps(SelectionKey.OP_WRITE);
                state = SENDING;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    class AsyncTask implements Runnable {
        @Override
        public void run() {
            // 在内部类的方法中，要指定某个嵌套层次的外围类的“this”引用时，使用“外围类名.this”语
            MultiThreadEchoHandler.this.asyncRun();
        }
    }
}
