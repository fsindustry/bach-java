package com.fsindustry.bach.core.schedule;

/**
 * 调度器接口
 */
public interface Scheduler {

    /**
     * 创建追加日志定时任务
     *
     * @param task 定时任务执行操作实现
     * @return 定时任务对象
     */
    LogReplicationTask scheduleLogReplicationTask(Runnable task);

    /**
     * 创建选举超时定时任务
     *
     * @param task 定时任务执行操作实现
     * @return 定时任务对象
     */
    ElectionTimeout scheduleElectionTimeout(Runnable task);

    /**
     * 停止调度器
     */
    void stop() throws InterruptedException;
}
