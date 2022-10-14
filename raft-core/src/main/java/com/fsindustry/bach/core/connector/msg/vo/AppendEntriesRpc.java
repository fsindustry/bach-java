package com.fsindustry.bach.core.connector.msg.vo;

import com.fsindustry.bach.core.log.entry.LogEntry;
import com.fsindustry.bach.core.node.model.NodeId;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.List;

/**
 * 追加日志rpc请求
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
    private List<LogEntry> entries = Collections.emptyList();

    /**
     * leader的commitIndex
     */
    private int leaderCommit;

    public int getLastEntryIndex() {
        return this.entries.isEmpty() ? this.prevLogIndex : this.entries.get(this.entries.size() - 1).getIndex();
    }
}
