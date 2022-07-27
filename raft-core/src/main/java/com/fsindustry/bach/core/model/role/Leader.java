package com.fsindustry.bach.core.model.role;

import com.fsindustry.bach.core.constant.RoleName;
import com.fsindustry.bach.core.schedule.LogReplicationTask;

public class Leader extends AbstractNodeRole {

    private final LogReplicationTask logReplicationTask;

    public Leader(RoleName name, Integer term, LogReplicationTask logReplicationTask) {
        super(RoleName.LEADER, term);
        this.logReplicationTask = logReplicationTask;
    }

    @Override
    public void cancelTimeoutOrTask() {
        logReplicationTask.cancel();
    }
}
