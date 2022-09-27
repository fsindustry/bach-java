package com.fsindustry.bach.core.schedule;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * 选举超时定时任务
 */
public class ElectionTimeout {

    public static final ElectionTimeout NONE = new ElectionTimeout(new NullScheduledFuture());

    /**
     * 存放定时任务执行结果，用来操作选举超时定时任务
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

    @Override
    public String toString() {

        if (scheduledFuture.isCancelled()) {
            return "ElectionTimeout(state=cancelled)";
        }

        if (scheduledFuture.isDone()) {
            return "ElectionTimeout(state=done)";
        }

        return "ElectionTimeout(delay= " + scheduledFuture.getDelay(TimeUnit.MILLISECONDS) + " ms)";
    }
}
