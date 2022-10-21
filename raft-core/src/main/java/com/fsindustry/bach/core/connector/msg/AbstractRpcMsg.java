package com.fsindustry.bach.core.connector.msg;

import com.fsindustry.bach.core.connector.channel.RaftChannel;
import com.fsindustry.bach.core.node.model.NodeId;
import lombok.Getter;

/**
 * rpc消息的抽象
 */
public abstract class AbstractRpcMsg<T> {

    @Getter
    private final T rpc;

    @Getter
    private final NodeId sourceNodeId;

    @Getter
    private final RaftChannel channel;

    public AbstractRpcMsg(T rpc, NodeId sourceNodeId, RaftChannel channel) {
        this.rpc = rpc;
        this.sourceNodeId = sourceNodeId;
        this.channel = channel;
    }
}
