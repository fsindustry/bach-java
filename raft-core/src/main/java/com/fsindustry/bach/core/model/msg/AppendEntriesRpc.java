package com.fsindustry.bach.core.model.msg;

import com.fsindustry.bach.core.node.model.NodeId;
import lombok.Data;

import java.util.Collections;
import java.util.List;

/**
 * 追加日志rpc请求
 */
@Data
public class AppendEntriesRpc {

    /**
     * 选举term
     */
    private int term;

    /**
     * leader节点ID
     */
    private NodeId leaderId;

    /**
     * 上一条日志的索引
     */
    private int prevLogIndex;

    /**
     * 上一条日志的term
     */
    private int prevLogTerm;

    /**
     * 待追加的日志条目
     */
    private List<Entry> entries = Collections.emptyList();

    /**
     * leader的commitIndex
     */
    private int leaderCommit;
}
