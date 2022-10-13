package com.fsindustry.bach.core.connector.msg;

import com.fsindustry.bach.core.connector.Channel;
import com.fsindustry.bach.core.connector.msg.vo.AppendEntriesRpc;
import com.fsindustry.bach.core.node.model.NodeId;

public class AppendEntriesRpcMsg extends AbstractRpcMsg<AppendEntriesRpc> {
    public AppendEntriesRpcMsg(AppendEntriesRpc rpc, NodeId sourceNodeId, Channel channel) {
        super(rpc, sourceNodeId, channel);
    }
}
