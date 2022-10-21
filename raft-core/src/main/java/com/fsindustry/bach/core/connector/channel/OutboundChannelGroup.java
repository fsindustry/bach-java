package com.fsindustry.bach.core.connector.channel;

import com.fsindustry.bach.core.connector.exception.ChannelConnectException;
import com.fsindustry.bach.core.connector.handler.MsgDispatchHandler;
import com.fsindustry.bach.core.connector.handler.RequestDecoder;
import com.fsindustry.bach.core.connector.handler.ResponseEncoder;
import com.fsindustry.bach.core.node.model.Address;
import com.fsindustry.bach.core.node.model.NodeId;
import com.google.common.eventbus.EventBus;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.concurrent.ThreadSafe;
import java.net.ConnectException;
import java.util.concurrent.*;

/**
 * 作为客户端，管理连向其它服务端的连接
 * 可以使用Netty自带的ChannelGroup替代
 */
@Slf4j
@ThreadSafe
public class OutboundChannelGroup {

    private final EventLoopGroup workerGroup;
    private final EventBus eventBus;
    private final NodeId selfNodeId;
    private final int connectTimeoutMillis;
    private final ConcurrentMap<NodeId, Future<NioRaftChannel>> channelMap = new ConcurrentHashMap<>();

    public OutboundChannelGroup(EventLoopGroup workerGroup, EventBus eventBus, NodeId selfNodeId, int logReplicationInterval) {
        this.workerGroup = workerGroup;
        this.eventBus = eventBus;
        this.selfNodeId = selfNodeId;
        this.connectTimeoutMillis = logReplicationInterval / 2;
    }

    public NioRaftChannel getOrConnect(NodeId nodeId, Address address) {
        Future<NioRaftChannel> future = channelMap.get(nodeId);
        if (future == null) {
            FutureTask<NioRaftChannel> newFuture = new FutureTask<>(() -> connect(nodeId, address));
            future = channelMap.putIfAbsent(nodeId, newFuture);
            if (future == null) {
                future = newFuture;
                newFuture.run();
            }
        }
        try {
            return future.get();
        } catch (Exception e) {
            channelMap.remove(nodeId);
            if (e instanceof ExecutionException) {
                Throwable cause = e.getCause();
                if (cause instanceof ConnectException) {
                    throw new ChannelConnectException("failed to get channel to node " + nodeId +
                            ", cause " + cause.getMessage(), cause);
                }
            }
            throw new ChannelException("failed to get channel to node " + nodeId, e);
        }
    }

    private NioRaftChannel connect(NodeId nodeId, Address address) throws InterruptedException {
        // 创建客户端连接
        Bootstrap bootstrap = new Bootstrap()
                .group(workerGroup)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectTimeoutMillis)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ChannelPipeline pipeline = ch.pipeline();
                        // 请求解析
                        pipeline.addLast(new RequestDecoder());
                        // 响应打包
                        pipeline.addLast(new ResponseEncoder());
                        // 业务逻辑处理
                        pipeline.addLast(new MsgDispatchHandler(eventBus, nodeId, selfNodeId));
                    }
                });
        ChannelFuture future = bootstrap.connect(address.getHost(), address.getPort()).sync();
        if (!future.isSuccess()) {
            throw new ChannelException("failed to connect", future.cause());
        }
        log.debug("channel OUTBOUND-{} connected", nodeId);
        Channel channel = future.channel();
        channel.closeFuture().addListener((ChannelFutureListener) cf -> {
            log.debug("channel OUTBOUND-{} disconnected", nodeId);
            channelMap.remove(nodeId);
        });
        return new NioRaftChannel(channel);
    }

    public void closeAll() {
        log.debug("close all outbound channels");
        channelMap.forEach((nodeId, nioChannelFuture) -> {
            try {
                nioChannelFuture.get().close();
            } catch (Exception e) {
                log.warn("failed to close", e);
            }
        });
    }

}
