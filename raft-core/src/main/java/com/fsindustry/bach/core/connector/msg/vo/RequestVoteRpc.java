package com.fsindustry.bach.core.connector.msg.vo;

import com.fsindustry.bach.core.node.model.NodeId;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 选举RPC请求
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RequestVoteRpc {

    /**
     * 选举term
     */
    private int term;

    /**
     * Candidate节点ID（发送者自己）
     */
    private NodeId candidateId;

    /**
     * Candidate最后一条日志
     */
    private int lastLogIndex = 0;

    /**
     * Candidate最后一条日志的term
     */
    private int lastLogTerm = 0;
}
