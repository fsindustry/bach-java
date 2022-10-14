package com.fsindustry.bach.core.schedule;

import com.fsindustry.bach.core.node.config.NodeConfig;

import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * 调度器默认实现
 */
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
     * 选举超时时间，随机部分生成器
     */
    private final Random electionTimeoutRandom;

    /**
     * 定时任务调度器
     */
    private final ScheduledExecutorService executorService;

    public DefaultScheduler(NodeConfig config) {

        this.minElectionTimeout = config.getMinElectionTimeout();
        this.maxElectionTimeout = config.getMaxElectionTimeout();
        this.logReplicationDelay = config.getLogReplicationDelay();
        this.logReplicationInterval = config.getLogReplicationInterval();

        if (minElectionTimeout <= 0 || maxElectionTimeout <= 0 || minElectionTimeout > maxElectionTimeout) {
            throw new IllegalArgumentException("");
        }

        if (logReplicationDelay < 0 || logReplicationInterval <= 0) {
            throw new IllegalArgumentException("");
        }

        electionTimeoutRandom = new Random();
        // 使用单线程调度器
        executorService = Executors.newSingleThreadScheduledExecutor(f -> new Thread(f, "scheduler"));
    }

    @Override
    public LogReplicationTask scheduleLogReplicationTask(Runnable task) {
        // 数据同步定时任务，以固定时间间隔运行
        ScheduledFuture<?> scheduledFuture = executorService.scheduleWithFixedDelay(task, logReplicationDelay, logReplicationInterval, TimeUnit.MILLISECONDS);
        return new LogReplicationTask(scheduledFuture);
    }

    @Override
    public ElectionTimeout scheduleElectionTimeout(Runnable task) {
        // 生成随机选举超时时间，在[minElectionTimeout, maxElectionTimeout]区间范围内
        int timeout = electionTimeoutRandom.nextInt(maxElectionTimeout - minElectionTimeout) + minElectionTimeout;
        // 每次都是单次调度，因为要随着AppendLogMsg不停的重置，故不能以固定周期调度
        ScheduledFuture<?> scheduledFuture = executorService.schedule(task, timeout, TimeUnit.MILLISECONDS);
        return new ElectionTimeout(scheduledFuture);
    }

    @Override
    public void stop() throws InterruptedException {
    }
}
