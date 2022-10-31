package com.fsindustry.bach.core.connector.handler;

import com.fsindustry.bach.core.connector.channel.InboundChannelGroup;
import com.fsindustry.bach.core.connector.channel.NioRaftChannel;
import com.fsindustry.bach.core.node.model.NodeId;
import com.google.common.eventbus.EventBus;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

/**
 * 服务端消息分发处理
 * 1）建链时，接收NodeId，标记连接；
 * 2）处理各类RPC请求，回复响应给客户端；
 */
@Slf4j
public class ServerMsgDispatchHandler extends AbstractHandler {

    /**
     * 客户端连接ChannelGroup
     */
    private final InboundChannelGroup channelGroup;


    public ServerMsgDispatchHandler(EventBus eventBus, InboundChannelGroup channelGroup) {
        super(eventBus);
        this.channelGroup = channelGroup;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        // 添加客户端连接
        if (msg instanceof NodeId) {
            remoteId = (NodeId) msg;
            NioRaftChannel nioChannel = new NioRaftChannel(ctx.channel());
            channel = nioChannel;
            channelGroup.add(remoteId, nioChannel);
            return;
        }

        log.debug("receive {} from {}", msg, remoteId);
        super.channelRead(ctx, msg);
    }
}
