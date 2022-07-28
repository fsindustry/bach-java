package com.fsindustry.bach.core.connector;

import com.fsindustry.bach.core.connector.model.AppendEntriesResult;
import com.fsindustry.bach.core.connector.model.AppendEntriesRpc;
import com.fsindustry.bach.core.connector.model.RequestVoteResult;
import com.fsindustry.bach.core.connector.model.RequestVoteRpc;
import com.fsindustry.bach.core.node.model.NodeEndpoint;

import java.util.Collection;

/**
 * RPC组件
 */
public interface Connector {

    void initialize();

    void sendRequestVote(RequestVoteRpc rpc, Collection<NodeEndpoint> dest);

    void replyRequestVote(RequestVoteResult result, NodeEndpoint dest);

    void sendAppendEntries(AppendEntriesRpc rpc, NodeEndpoint dest);

    void replyAppendEntries(AppendEntriesResult result, NodeEndpoint dest);

    /**
     * 关闭链接器
     */
    void close();
}
