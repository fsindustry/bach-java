package com.fsindustry.bach.core.node.role;

import com.fsindustry.bach.core.node.model.NodeId;
import com.fsindustry.bach.core.schedule.LogReplicationTask;

public class Leader extends AbstractNodeRole {

    /**
     * 日志复制定时器
     */
    private final LogReplicationTask logReplicationTask;

    public Leader(Integer term, LogReplicationTask logReplicationTask) {
        super(RoleName.LEADER, term);
        this.logReplicationTask = logReplicationTask;
    }

    @Override
    public void cancelTimeoutOrTask() {
        logReplicationTask.cancel();
    }

    @Override
    public NodeId getLeaderId(NodeId selfId) {
        return null;
    }
}
