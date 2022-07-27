package com.fsindustry.bach.core.schedule;

import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class DefaultScheduler implements Scheduler {

    /**
     * 最小选举超时时间
     */
    private final int minElectionTimeout;

    /**
     * 最大选举超时时间
     */
    private final int maxElectionTimeout;

    /**
     * 初次日志复制延迟时间
     */
    private final int logReplicationDelay;

    /**
     * 追加消息心跳间隔时间
     */
    private final int logReplicationInterval;

    /**
     * 选举超时时间，随几部分生成器
     */
    private final Random electionTimeoutRandom;

    /**
     * 定时任务调度器
     */
    private final ScheduledExecutorService executorService;

    public DefaultScheduler(int minElectionTimeout, int maxElectionTimeout, int logReplicationDelay, int logReplicationInterval) {

        this.minElectionTimeout = minElectionTimeout;
        this.maxElectionTimeout = maxElectionTimeout;
        this.logReplicationDelay = logReplicationDelay;
        this.logReplicationInterval = logReplicationInterval;

        if (minElectionTimeout <= 0 || maxElectionTimeout <= 0 || minElectionTimeout > maxElectionTimeout) {
            throw new IllegalArgumentException("");
        }

        if (logReplicationDelay < 0 || logReplicationInterval <= 0) {
            throw new IllegalArgumentException("");
        }

        electionTimeoutRandom = new Random();
        executorService = Executors.newSingleThreadScheduledExecutor(f -> new Thread(f, "scheduler"));
    }

    @Override
    public LogReplicationTask scheduleLogReplicationTask(Runnable task) {
        ScheduledFuture<?> scheduledFuture = executorService.scheduleWithFixedDelay(task, logReplicationDelay, logReplicationInterval, TimeUnit.MILLISECONDS);
        return new LogReplicationTask(scheduledFuture);
    }

    @Override
    public ElectionTimeout scheduleElectionTimeout(Runnable task) {
        // 生成随机选举超时时间，在[minElectionTimeout, maxElectionTimeout]区间范围内
        int timeout = electionTimeoutRandom.nextInt(maxElectionTimeout - minElectionTimeout) + minElectionTimeout;
        ScheduledFuture<?> scheduledFuture = executorService.schedule(task, timeout, TimeUnit.MILLISECONDS);
        return new ElectionTimeout(scheduledFuture);
    }

    @Override
    public void stop() throws InterruptedException {
    }
}
