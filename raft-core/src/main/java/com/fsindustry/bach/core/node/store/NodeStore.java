package com.fsindustry.bach.core.node.store;

import com.fsindustry.bach.core.node.model.NodeId;

/**
 * 节点状态存储组件
 */
public interface NodeStore {

    /**
     * 获取currentTerm
     */
    int getTerm();

    /**
     * 设置currentTerm
     */
    void setTerm(int term);

    /**
     * 获取votedFor
     */
    NodeId getVotedFor();

    /**
     * 设置votedFor
     */
    void setVotedFor(NodeId votedFor);

    /**
     * 关闭文件
     */
    void close();
}
