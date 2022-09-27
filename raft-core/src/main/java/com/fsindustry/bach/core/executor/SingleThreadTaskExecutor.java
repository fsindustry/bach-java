package com.fsindustry.bach.core.executor;

import java.util.concurrent.*;

/**
 * 单线程执行器
 */
public class SingleThreadTaskExecutor implements TaskExecutor {

    private final ExecutorService executorService;

    public SingleThreadTaskExecutor() {
        this(Executors.defaultThreadFactory());
    }

    public SingleThreadTaskExecutor(String name) {
        this(r -> new Thread(name));
    }

    public SingleThreadTaskExecutor(ThreadFactory factory) {
        // 单线程执行器
        executorService = Executors.newSingleThreadExecutor(factory);
    }

    @Override
    public Future<?> submit(Runnable task) {
        return executorService.submit(task);
    }

    @Override
    public <V> Future<V> submit(Callable<V> task) {
        return executorService.submit(task);
    }

    @Override
    public void shutdown() throws InterruptedException {
        executorService.shutdown();
        executorService.awaitTermination(1, TimeUnit.SECONDS);
    }
}
