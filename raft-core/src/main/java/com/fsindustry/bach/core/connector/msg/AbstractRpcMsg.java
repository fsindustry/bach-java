package com.fsindustry.bach.core.connector.msg;

import com.fsindustry.bach.core.connector.Channel;
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
    private final Channel channel;

    public AbstractRpcMsg(T rpc, NodeId sourceNodeId, Channel channel) {
        this.rpc = rpc;
        this.sourceNodeId = sourceNodeId;
        this.channel = channel;
    }
}
