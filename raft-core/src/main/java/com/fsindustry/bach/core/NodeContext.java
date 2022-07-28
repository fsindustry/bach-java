package com.fsindustry.bach.core;

import com.fsindustry.bach.core.connector.Connector;
import com.fsindustry.bach.core.executor.TaskExecutor;
import com.fsindustry.bach.core.log.Log;
import com.fsindustry.bach.core.node.model.NodeGroup;
import com.fsindustry.bach.core.node.model.NodeId;
import com.fsindustry.bach.core.node.store.NodeStore;
import com.fsindustry.bach.core.schedule.Scheduler;
import com.google.common.eventbus.EventBus;
import lombok.Data;

/**
 * 节点状态上下文
 */
@Data
public class NodeContext {

    /**
     * 当前节点ID
     */
    private NodeId selfId;

    /**
     * 当前group成员列表
     */
    private NodeGroup group;

    /**
     * 日志组件
     */
    private Log log;

    /**
     * rpc组件
     */
    private Connector connector;

    /**
     * 定时器组件
     */
    private Scheduler scheduler;

    /**
     * 组件间消息总线
     */
    private EventBus eventBus;

    /**
     * raft状态机主线程
     */
    private TaskExecutor taskExecutor;

    /**
     * raft状态数据存储
     */
    private NodeStore store;
}
