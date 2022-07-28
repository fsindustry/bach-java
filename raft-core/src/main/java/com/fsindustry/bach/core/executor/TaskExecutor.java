package com.fsindustry.bach.core.executor;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;

/**
 * raft状态机主线程
 */
public interface TaskExecutor {

    /**
     * 提交不带返回值的任务
     */
    Future<?> submit(Runnable task);

    /**
     * 提交带返回值的任务
     */
    <V> Future<V> submit(Callable<V> task);

    /**
     * 关闭执行器
     */
    void shutdown() throws InterruptedException;
}
