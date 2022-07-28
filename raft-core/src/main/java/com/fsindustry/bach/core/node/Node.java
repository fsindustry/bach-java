package com.fsindustry.bach.core.node;

/**
 * raft状态机的核心实现
 */
public interface Node {

    // 启动
    void start();

    // 停止
    void stop() throws InterruptedException;
}
