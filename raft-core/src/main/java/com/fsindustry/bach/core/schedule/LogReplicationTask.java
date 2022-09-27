package com.fsindustry.bach.core.schedule;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * 日志复制定时任务
 */
public class LogReplicationTask {

    private final ScheduledFuture<?> scheduledFuture;

    public LogReplicationTask(ScheduledFuture<?> scheduledFuture) {
        this.scheduledFuture = scheduledFuture;
    }

    public void cancel() {
        scheduledFuture.cancel(false);
    }

    @Override
    public String toString() {
        return "LogReplicationTask(delay=" + scheduledFuture.getDelay(TimeUnit.MILLISECONDS) + ")";
    }
}
