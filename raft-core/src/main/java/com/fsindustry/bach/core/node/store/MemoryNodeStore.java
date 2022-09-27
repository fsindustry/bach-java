package com.fsindustry.bach.core.node.store;

import com.fsindustry.bach.core.node.model.NodeId;
import lombok.Getter;
import lombok.Setter;

/**
 * 基于内存保存Node状态
 */
public class MemoryNodeStore implements NodeStore {

    /**
     * 当前任期
     */
    @Getter
    @Setter
    private int term;

    /**
     * 上一次投票给谁
     */
    @Getter
    @Setter
    private NodeId votedFor;

    public MemoryNodeStore() {
        this(0, null);
    }

    public MemoryNodeStore(int term, NodeId votedFor) {
        this.term = term;
        this.votedFor = votedFor;
    }

    @Override
    public void close() {

    }
}
