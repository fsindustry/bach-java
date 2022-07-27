package com.fsindustry.bach.core.schedule;

import java.util.concurrent.ScheduledFuture;

/**
 * 选举超时定时任务
 */
public class ElectionTimeout {

    /**
     * 存放定时任务执行结果
     */
    private final ScheduledFuture<?> scheduledFuture;

    public ElectionTimeout(ScheduledFuture<?> scheduledFuture) {
        this.scheduledFuture = scheduledFuture;
    }

    /**
     * 取消选举超时
     */
    public void cancel() {
        scheduledFuture.cancel(false);
    }

}
