package com.crazymakercircle.iodemo.udpDemos;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;

public class UDPServer {
    public void receive() throws Exception {
        DatagramChannel datagramChannel = DatagramChannel.open();

        datagramChannel.configureBlocking(false);

        InetSocketAddress binAddress = new InetSocketAddress("127.0.0.1", 8299);
        datagramChannel.bind(binAddress);

        Selector selector = Selector.open();
        datagramChannel.register(selector, SelectionKey.OP_READ);

        // 通过选择器，查询IO事件
        while (selector.select() > 0) {
            Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();

            ByteBuffer buffer = ByteBuffer.allocate(1024);

            while (iterator.hasNext()) {
                SelectionKey selectionKey =  iterator.next();
                if (selectionKey.isReadable()) {
                    SocketAddress client = datagramChannel.receive(buffer);

                    buffer.flip();
                    String msg = new String(buffer.array(), 0, buffer.limit());
                    System.out.println(msg);

                    buffer.clear();
                }
            }

            // 关键一步：不移除的话，程序无法重复接受信息
            iterator.remove();
        }
        selector.close();
        datagramChannel.close();
    }

    public static void main(String[] args) throws Exception {
        new UDPServer().receive();
    }
}
