package com.fsindustry.bach.core.connector.handler;

import com.fsindustry.bach.core.connector.channel.NioRaftChannel;
import com.fsindustry.bach.core.node.model.NodeId;
import com.google.common.eventbus.EventBus;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

/**
 * 客户端消息分发处理
 * 1）建链后，发送NodeId
 * 2）回复各类RPC响应
 */
@Slf4j
public class ClientMsgDispatchHandler extends AbstractHandler {

    private final NodeId selfNodeId;


    public ClientMsgDispatchHandler(EventBus eventBus, NodeId remoteId, NodeId selfNodeId) {
        super(eventBus);
        this.remoteId = remoteId;
        this.selfNodeId = selfNodeId;
    }

    /**
     * 建链时，保存NodeId
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        ctx.write(selfNodeId);
        channel = new NioRaftChannel(ctx.channel());
    }

    /**
     * 读取消息，不作任何操作
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        log.debug("receive {} from {}", msg, remoteId);
        super.channelRead(ctx, msg);
    }
}
