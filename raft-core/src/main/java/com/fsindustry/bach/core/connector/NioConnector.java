package com.fsindustry.bach.core.connector;

import com.fsindustry.bach.core.connector.channel.InboundChannelGroup;
import com.fsindustry.bach.core.connector.channel.OutboundChannelGroup;
import com.fsindustry.bach.core.connector.exception.ChannelConnectException;
import com.fsindustry.bach.core.connector.exception.ConnectorException;
import com.fsindustry.bach.core.connector.handler.MsgDispatchHandler;
import com.fsindustry.bach.core.connector.handler.RequestDecoder;
import com.fsindustry.bach.core.connector.handler.ResponseEncoder;
import com.fsindustry.bach.core.connector.msg.AppendEntriesRpcMsg;
import com.fsindustry.bach.core.connector.msg.RequestVoteRpcMsg;
import com.fsindustry.bach.core.connector.msg.vo.AppendEntriesResult;
import com.fsindustry.bach.core.connector.msg.vo.AppendEntriesRpc;
import com.fsindustry.bach.core.connector.msg.vo.RequestVoteResult;
import com.fsindustry.bach.core.connector.msg.vo.RequestVoteRpc;
import com.fsindustry.bach.core.node.model.NodeEndpoint;
import com.fsindustry.bach.core.node.model.NodeId;
import com.google.common.eventbus.EventBus;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.concurrent.ThreadSafe;
import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@ThreadSafe
public class NioConnector implements Connector {

    /**
     * acceptor线程池
     */
    private final NioEventLoopGroup bossGroup = new NioEventLoopGroup(1);

    /**
     * worker线程池
     */
    private final NioEventLoopGroup workerGroup;

    /**
     * 是否共享workerGroup
     * 因为节点可能即做客户端，又做服务端，因此可能复用workerGroup
     */
    private final boolean workerGroupShared;

    /**
     * 消息总线
     */
    private final EventBus eventBus;

    /**
     * 消息端口
     */
    private int port;

    /**
     * 作为服务端，管理所有客户端连接
     */
    private final InboundChannelGroup inboundChannelGroup = new InboundChannelGroup();

    /**
     * 作为客户端，管理所有服务端连接
     */
    private final OutboundChannelGroup outboundChannelGroup;

    public NioConnector(NioEventLoopGroup workerGroup, boolean workerGroupShared,
                        NodeId selfNodeId, EventBus eventBus,
                        int port, int logReplicationInterval) {
        this.workerGroup = workerGroup;
        this.workerGroupShared = workerGroupShared;
        this.eventBus = eventBus;
        this.port = port;
        outboundChannelGroup = new OutboundChannelGroup(workerGroup, eventBus, selfNodeId, logReplicationInterval);
    }

    private final ExecutorService executorService = Executors.newCachedThreadPool((r) -> {
        Thread thread = new Thread(r);
        thread.setUncaughtExceptionHandler((t, e) -> {
            logException(e);
        });
        return thread;
    });

    private void logException(Throwable e) {
        if (e instanceof ChannelConnectException) {
            log.warn(e.getMessage());
        } else {
            log.warn("failed to process channel", e);
        }
    }


    @Override
    public void initialize() {
        // 创建服务端，开启监听
        ServerBootstrap serverBootstrap = new ServerBootstrap()
                .group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast(new RequestDecoder());
                        pipeline.addLast(new ResponseEncoder());
                        pipeline.addLast(new MsgDispatchHandler(eventBus, inboundChannelGroup));
                    }
                });
        log.debug("node listen on port {}", port);
        try {
            serverBootstrap.bind(port).sync();
        } catch (InterruptedException e) {
            throw new ConnectorException("failed to bind port", e);
        }
    }

    @Override
    public void sendRequestVote(RequestVoteRpc rpc, Collection<NodeEndpoint> dest) {

    }

    @Override
    public void replyRequestVote(RequestVoteResult result, RequestVoteRpcMsg dest) {

    }

    @Override
    public void sendAppendEntries(AppendEntriesRpc rpc, NodeEndpoint dest) {

    }

    @Override
    public void replyAppendEntries(AppendEntriesResult result, AppendEntriesRpcMsg rpcMessage) {

    }

    @Override
    public void resetChannels() {

    }

    @Override
    public void close() {
        log.debug("close connector");
        inboundChannelGroup.closeAll();
        outboundChannelGroup.closeAll();
        // 先关闭连接接收端，再关闭工作线程
        bossGroup.shutdownGracefully();
        if (!workerGroupShared) {
            workerGroup.shutdownGracefully();
        }
    }
}
