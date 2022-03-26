package com.crazymakercircle.iodemo.udpDemos;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class UDPClient {
    public void send() throws Exception {
        DatagramChannel dChannel = DatagramChannel.open();
        dChannel.configureBlocking(false);
        ByteBuffer buffer = ByteBuffer.allocate(1024);

        Scanner scanner = new Scanner(System.in);

        InetSocketAddress ep = new InetSocketAddress("127.0.0.1", 8299);

        while (scanner.hasNext()) {
            String next = scanner.next();
            buffer.put((System.currentTimeMillis() + ">>" + next).getBytes(StandardCharsets.UTF_8));

            buffer.flip();
            dChannel.send(buffer, ep);

            buffer.clear();
        }
        dChannel.close();
    }

    public static void main(String[] args) throws Exception {
        new UDPClient().send();
    }
}
