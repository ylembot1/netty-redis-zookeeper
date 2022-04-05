package com.crazymakercircle.netty.basic;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoop;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoop;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class NettyDiscardServer {
    private final int serverPort;
    ServerBootstrap b = new ServerBootstrap();
    public NettyDiscardServer(int port) {
        this.serverPort = port;
    }

    public void runServer() {
        // 创建反应器线程组
        EventLoopGroup bossLoopGroup = new NioEventLoopGroup(1);
        EventLoopGroup workLoopGroup = new NioEventLoopGroup();

        try {
            // 1. 设置反应器线程组
            b.group(bossLoopGroup, workLoopGroup);

            // 2. 设置nio类型的通道
            b.channel(NioServerSocketChannel.class);

            // 3. 设置监听端口
            b.localAddress(serverPort);

            // 4. 设置通道的参数
            b.option(ChannelOption.SO_KEEPALIVE, true);
            b.option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
            // b.option(ChannelOption.SO_BACKLOG, 300);

            // 5. 装配子通道流水线
            b.childHandler(new ChannelInitializer<SocketChannel>() {
                // 有连接到达时会创建一个通道
                protected void initChannel(SocketChannel ch) throws Exception {
                    // 流水线管理子通道中的Handler处理器
                    // 向子通道流水线添加一个handler处理器
                    ch.pipeline().addLast(new NettyDiscardHandler());
                }
            });

            // 6. 开始绑定服务器
            // 通过调用sync同步方法阻塞，直到绑定成功
            ChannelFuture channelFuture = b.bind().sync();
            System.out.println("服务器启动成功，监听端口：" + channelFuture.channel().localAddress());

            // 7. 等待通道关闭的异步任务结束
            ChannelFuture closeFuture = channelFuture.channel().closeFuture();
            closeFuture.sync();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            workLoopGroup.shutdownGracefully();
            bossLoopGroup.shutdownGracefully();
        }
    }

    public static void main(String[] args) {
        int port = 8080;
        new NettyDiscardServer(port).runServer();
    }
}
