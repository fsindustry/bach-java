package com.fsindustry.bach.core.connector.handler;

import com.fsindustry.bach.core.connector.channel.RaftChannel;
import com.fsindustry.bach.core.connector.msg.AppendEntriesResultMsg;
import com.fsindustry.bach.core.connector.msg.AppendEntriesRpcMsg;
import com.fsindustry.bach.core.connector.msg.RequestVoteRpcMsg;
import com.fsindustry.bach.core.connector.msg.vo.AppendEntriesResult;
import com.fsindustry.bach.core.connector.msg.vo.AppendEntriesRpc;
import com.fsindustry.bach.core.connector.msg.vo.RequestVoteResult;
import com.fsindustry.bach.core.connector.msg.vo.RequestVoteRpc;
import com.fsindustry.bach.core.node.model.NodeId;
import com.google.common.eventbus.EventBus;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import lombok.extern.slf4j.Slf4j;

/**
 * 公共消息处理逻辑抽象
 */
@Slf4j
public class AbstractHandler extends ChannelDuplexHandler {

    protected final EventBus eventBus;
    protected NodeId remoteId;
    protected RaftChannel channel;
    private AppendEntriesRpc lastAppendEntriesRpc;

    AbstractHandler(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    /**
     * 读取消息
     * 都交由eventBus通知核心处理线程处理
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        assert remoteId != null;
        assert channel != null;

        if (msg instanceof RequestVoteRpc) {
            RequestVoteRpc rpc = (RequestVoteRpc) msg;
            eventBus.post(new RequestVoteRpcMsg(rpc, remoteId, channel));
        } else if (msg instanceof RequestVoteResult) {
            eventBus.post(msg);
        } else if (msg instanceof AppendEntriesRpc) {
            AppendEntriesRpc rpc = (AppendEntriesRpc) msg;
            eventBus.post(new AppendEntriesRpcMsg(rpc, remoteId, channel));
        } else if (msg instanceof AppendEntriesResult) {
            AppendEntriesResult result = (AppendEntriesResult) msg;
            if (lastAppendEntriesRpc == null) {
                log.warn("no last append entries rpc");
            } else {
                eventBus.post(new AppendEntriesResultMsg(result, remoteId, lastAppendEntriesRpc));
                lastAppendEntriesRpc = null;
            }
        }
    }

    /**
     * 响应结果
     * 仅记录写入状态，不干涉响应过程
     */
    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (msg instanceof AppendEntriesRpc) {
            lastAppendEntriesRpc = (AppendEntriesRpc) msg;
        }
        super.write(ctx, msg, promise);
    }

    /**
     * 异常处理
     * 关闭连接，并通知ChannelFuture；
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.warn(cause.getMessage(), cause);
        ctx.close();
    }
}
