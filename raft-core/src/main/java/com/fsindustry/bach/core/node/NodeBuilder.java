package com.fsindustry.bach.core.node;

import com.fsindustry.bach.core.NodeContext;
import com.fsindustry.bach.core.connector.Connector;
import com.fsindustry.bach.core.connector.NioConnector;
import com.fsindustry.bach.core.executor.SingleThreadTaskExecutor;
import com.fsindustry.bach.core.executor.TaskExecutor;
import com.fsindustry.bach.core.log.FileLog;
import com.fsindustry.bach.core.log.Log;
import com.fsindustry.bach.core.log.MemoryLog;
import com.fsindustry.bach.core.node.config.NodeConfig;
import com.fsindustry.bach.core.node.model.NodeEndpoint;
import com.fsindustry.bach.core.node.model.NodeGroup;
import com.fsindustry.bach.core.node.model.NodeId;
import com.fsindustry.bach.core.node.store.FileNodeStore;
import com.fsindustry.bach.core.node.store.NodeStore;
import com.fsindustry.bach.core.schedule.DefaultScheduler;
import com.fsindustry.bach.core.schedule.Scheduler;
import com.google.common.eventbus.EventBus;
import io.netty.channel.nio.NioEventLoopGroup;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.annotation.Nullable;
import java.io.File;
import java.util.Collection;
import java.util.Collections;

/**
 * 帮助构建Node
 */
@Data
@Accessors(fluent = true)
public class NodeBuilder {

    private final NodeGroup group;
    private final NodeId selfId;
    private final EventBus eventBus;
    private NodeConfig config = new NodeConfig();
    private boolean standby = false;
    private Log log;
    private NodeStore store;
    private Scheduler scheduler;
    private Connector connector;
    private TaskExecutor taskExecutor;
    private NioEventLoopGroup workGroup;

    public NodeBuilder(NodeEndpoint endpoint) {
        this(Collections.singletonList(endpoint), endpoint.getId());
    }

    public NodeBuilder(Collection<NodeEndpoint> endpoints, NodeId selfId) {
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

    public NodeBuilder setDataDir(@Nullable String dataDirPath) {
        if (dataDirPath == null || dataDirPath.isEmpty()) {
            return this;
        }
        File dataDir = new File(dataDirPath);
        if (!dataDir.isDirectory() || !dataDir.exists()) {
            throw new IllegalArgumentException("[" + dataDirPath + "] not a directory, or not exists");
        }
        log = new FileLog(dataDir, eventBus);
        store = new FileNodeStore(new File(dataDir, FileNodeStore.FILE_NAME));
        return this;
    }
}
