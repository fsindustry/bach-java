package com.fsindustry.bach.core.connector.msg;

import com.fsindustry.bach.core.connector.msg.vo.AppendEntriesResult;
import com.fsindustry.bach.core.connector.msg.vo.AppendEntriesRpc;
import com.fsindustry.bach.core.node.model.NodeId;
import lombok.Value;

/**
 * 存放AppendEntriesRpc及其响应结果
 */
@Value
public class AppendEntriesResultMsg {

    /**
     * AppendEntriesRpc响应结果
     */
    AppendEntriesResult result;

    /**
     * 发起请求的nodeId
     */
    NodeId sourceNodeId;

    /**
     * AppendEntriesRpc
     */
    AppendEntriesRpc rpc;
}