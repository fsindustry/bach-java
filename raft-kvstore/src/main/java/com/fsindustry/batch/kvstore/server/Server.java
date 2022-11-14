package com.fsindustry.batch.kvstore.server;

import com.fsindustry.bach.core.node.Node;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Server {

    private final Node node;
    private final int port;
    private final Service service;
    private final NioEventLoopGroup bossGroup = new NioEventLoopGroup(1);
    private final NioEventLoopGroup workerGroup = new NioEventLoopGroup(4);

    public Server(Node node, int port) {
        this.node = node;
        this.service = new Service(node);
        this.port = port;
    }

    public void start() throws Exception {
        this.node.start();

        ServerBootstrap serverBootstrap = new ServerBootstrap()
                .group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ChannelPipeline pipeline = ch.pipeline();
                        // 请求解析
                        pipeline.addLast(new ClientEncoder());
                        // 响应打包
                        pipeline.addLast(new ClientDecoder());
                        // 消息处理
                        pipeline.addLast(new ServiceHandler(service));
                    }
                });
        log.info("server started at port {}", this.port);
        serverBootstrap.bind(this.port);
    }

    public void stop() throws Exception {
        log.info("stopping server");
        this.node.stop();
        this.workerGroup.shutdownGracefully();
        this.bossGroup.shutdownGracefully();
    }

}
