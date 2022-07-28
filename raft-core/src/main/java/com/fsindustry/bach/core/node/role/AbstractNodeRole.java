package com.fsindustry.bach.core.node.role;

import com.fsindustry.bach.core.node.model.NodeId;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public abstract class AbstractNodeRole {

    @Getter
    private final RoleName name;

    @Getter
    private final Integer term;

    /**
     * 取消 选举超时 / 心跳超时定时器
     */
    public abstract void cancelTimeoutOrTask();

    /**
     * 获取当前group的leaderId
     */
    public abstract NodeId getLeaderId(NodeId selfId);
}
