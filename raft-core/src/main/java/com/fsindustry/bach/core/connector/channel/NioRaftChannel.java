package com.fsindustry.bach.core.connector.channel;

import com.fsindustry.bach.core.connector.msg.vo.AppendEntriesResult;
import com.fsindustry.bach.core.connector.msg.vo.AppendEntriesRpc;
import com.fsindustry.bach.core.connector.msg.vo.RequestVoteResult;
import com.fsindustry.bach.core.connector.msg.vo.RequestVoteRpc;
import io.netty.channel.Channel;
import io.netty.channel.ChannelException;

import javax.annotation.Nonnull;

/**
 * 基于NIO的Raft连接
 * 只做消息发送的封装
 */
public class NioRaftChannel implements RaftChannel {

    private final Channel channel;

    public NioRaftChannel(io.netty.channel.Channel channel) {
        this.channel = channel;
    }

    @Override
    public void writeRequestVoteRpc(@Nonnull RequestVoteRpc rpc) {
        channel.writeAndFlush(rpc);
    }

    @Override
    public void writeRequestVoteResult(@Nonnull RequestVoteResult result) {
        channel.writeAndFlush(result);
    }

    @Override
    public void writeAppendEntriesRpc(@Nonnull AppendEntriesRpc rpc) {
        channel.writeAndFlush(rpc);
    }

    @Override
    public void writeAppendEntriesResult(@Nonnull AppendEntriesResult result) {
        channel.writeAndFlush(result);
    }

    @Override
    public void close() {
        try {
            channel.close().sync();
        } catch (InterruptedException e) {
            throw new ChannelException("failed to close", e);
        }
    }

    Channel getDelegate() {
        return channel;
    }

}