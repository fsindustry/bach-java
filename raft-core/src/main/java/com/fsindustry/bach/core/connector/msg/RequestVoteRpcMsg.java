package com.fsindustry.bach.core.connector.msg;

import com.fsindustry.bach.core.connector.channel.RaftChannel;
import com.fsindustry.bach.core.connector.msg.vo.RequestVoteRpc;
import com.fsindustry.bach.core.node.model.NodeId;

public class RequestVoteRpcMsg extends AbstractRpcMsg<RequestVoteRpc> {
    public RequestVoteRpcMsg(RequestVoteRpc rpc, NodeId sourceNodeId, RaftChannel channel) {
        super(rpc, sourceNodeId, channel);
    }
}
