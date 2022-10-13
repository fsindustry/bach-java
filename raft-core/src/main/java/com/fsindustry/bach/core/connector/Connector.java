package com.fsindustry.bach.core.connector;

import com.fsindustry.bach.core.connector.msg.AppendEntriesRpcMsg;
import com.fsindustry.bach.core.connector.msg.RequestVoteRpcMsg;
import com.fsindustry.bach.core.connector.msg.vo.AppendEntriesResult;
import com.fsindustry.bach.core.connector.msg.vo.AppendEntriesRpc;
import com.fsindustry.bach.core.connector.msg.vo.RequestVoteResult;
import com.fsindustry.bach.core.connector.msg.vo.RequestVoteRpc;
import com.fsindustry.bach.core.node.model.NodeEndpoint;

import java.util.Collection;

/**
 * RPC组件
 */
public interface Connector {

    /**
     * 初始化
     */
    void initialize();

    /**
     * 发送RequestVoteRpc给多个Node
     */
    void sendRequestVote(RequestVoteRpc rpc, Collection<NodeEndpoint> dest);

    /**
     * 回复RequestVoteResult给对应节点
     */
    void replyRequestVote(RequestVoteResult result, RequestVoteRpcMsg dest);

    /**
     * 发送AppendEntriesRpc给目标节点
     * 因为每个Node同步进度不一样，因此不能同时发送
     */
    void sendAppendEntries(AppendEntriesRpc rpc, NodeEndpoint dest);

    /**
     * 回复AppendEntriesResult到目标节点
     */
    void replyAppendEntries(AppendEntriesResult result, AppendEntriesRpcMsg rpcMessage);

    /**
     * 关闭链接器
     */
    void close();
}
