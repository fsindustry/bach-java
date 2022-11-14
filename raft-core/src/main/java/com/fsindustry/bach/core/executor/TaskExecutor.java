package com.fsindustry.bach.core.executor;

import com.google.common.util.concurrent.FutureCallback;

import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

/**
 * raft状态机主线程
 */
public interface TaskExecutor {

    /**
     * Submit task.
     */
    Future<?> submit(Runnable task);

    /**
     * Submit callable task.
     */
    <V> Future<V> submit(Callable<V> task);

    /**
     * Submit task with callback.
     */
    void submit(Runnable task, FutureCallback<Object> callback);

    /**
     * Submit task with callbacks.
     */
    void submit(Runnable task, Collection<FutureCallback<Object>> callbacks);

    /**
     * Shutdown.
     */
    void shutdown() throws InterruptedException;
}
