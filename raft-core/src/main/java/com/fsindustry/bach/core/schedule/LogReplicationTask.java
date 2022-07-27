package com.fsindustry.bach.core.schedule;

import java.util.concurrent.ScheduledFuture;

public class LogReplicationTask {

    private final ScheduledFuture<?> scheduledFuture;

    public LogReplicationTask(ScheduledFuture<?> scheduledFuture) {
        this.scheduledFuture = scheduledFuture;
    }

    public void cancel() {
        scheduledFuture.cancel(false);
    }
}
