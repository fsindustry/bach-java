package com.fsindustry.bach.core.node;

import com.fsindustry.bach.core.NodeContext;
import com.fsindustry.bach.core.connector.Connector;
import com.fsindustry.bach.core.connector.NioConnector;
import com.fsindustry.bach.core.executor.SingleThreadTaskExecutor;
import com.fsindustry.bach.core.executor.TaskExecutor;
import com.fsindustry.bach.core.log.Log;
import com.fsindustry.bach.core.log.MemoryLog;
import com.fsindustry.bach.core.node.config.NodeConfig;
import com.fsindustry.bach.core.node.model.NodeEndpoint;
import com.fsindustry.bach.core.node.model.NodeGroup;
import com.fsindustry.bach.core.node.model.NodeId;
import com.fsindustry.bach.core.node.store.NodeStore;
import com.fsindustry.bach.core.schedule.DefaultScheduler;
import com.fsindustry.bach.core.schedule.Scheduler;
import com.google.common.eventbus.EventBus;
import io.netty.channel.nio.NioEventLoopGroup;
import lombok.experimental.Accessors;

import java.util.Collections;
import java.util.List;

/**
 * 帮助构建Node
 */
public class NodeBuilder {

    private final NodeGroup group;
    private final NodeId selfId;
    private final EventBus eventBus;
    @Accessors
    private NodeConfig config = new NodeConfig();
    private boolean standby = false;
    @Accessors
    private Log log;
    @Accessors
    private NodeStore store;
    @Accessors
    private Scheduler scheduler;
    @Accessors
    private Connector connector;
    @Accessors
    private TaskExecutor taskExecutor;
    @Accessors
    private NioEventLoopGroup workGroup;


    public NodeBuilder(NodeEndpoint endpoint) {
        this(Collections.singletonList(endpoint), endpoint.getId());
    }

    public NodeBuilder(List<NodeEndpoint> endpoints, NodeId selfId) {
        this.group = new NodeGroup(endpoints, selfId);
        this.selfId = selfId;
        this.eventBus = new EventBus(selfId.getValue());
    }

    public Node build() {
        return new NodeImpl(buildContext());
    }

    private NodeContext buildContext() {
        NodeContext context = new NodeContext();
        context.setGroup(group);
        context.setSelfId(selfId);
        context.setEventBus(eventBus);
        context.setScheduler(scheduler == null ? new DefaultScheduler(config) : scheduler);
        context.setConnector(connector != null ? connector : createNioConnector());
        context.setTaskExecutor(taskExecutor == null ? new SingleThreadTaskExecutor("node") : taskExecutor);
        context.setLog(log == null ? new MemoryLog() : log);
        return context;
    }

    private Connector createNioConnector() {
        int port = group.findSelf().getEndpoint().getPort();
        if (workGroup != null) {
            return new NioConnector(workGroup, true, selfId, eventBus, port, config.getLogReplicationInterval());
        }
        return new NioConnector(new NioEventLoopGroup(config.getNioWorkerThreads()), false,
                selfId, eventBus, port, config.getLogReplicationInterval());
    }
}
