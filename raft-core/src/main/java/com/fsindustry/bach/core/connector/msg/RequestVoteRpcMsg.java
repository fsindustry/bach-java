package com.fsindustry.bach.core.connector.msg;

import com.fsindustry.bach.core.connector.Channel;
import com.fsindustry.bach.core.connector.msg.vo.RequestVoteRpc;
import com.fsindustry.bach.core.node.model.NodeId;

public class RequestVoteRpcMsg extends AbstractRpcMsg<RequestVoteRpc> {
    public RequestVoteRpcMsg(RequestVoteRpc rpc, NodeId sourceNodeId, Channel channel) {
        super(rpc, sourceNodeId, channel);
    }
}
