package com.fsindustry.bach.core.node;

import com.fsindustry.bach.core.NodeContext;
import com.fsindustry.bach.core.node.role.RoleName;
import com.fsindustry.bach.core.connector.model.RequestVoteRpc;
import com.fsindustry.bach.core.connector.msg.RequestVoteRpcMsg;
import com.fsindustry.bach.core.node.role.AbstractNodeRole;
import com.fsindustry.bach.core.node.role.Candidate;
import com.fsindustry.bach.core.node.role.Follower;
import com.fsindustry.bach.core.node.store.NodeStore;
import com.fsindustry.bach.core.schedule.ElectionTimeout;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NodeImpl implements Node {

    /**
     * 节点状态上下文
     */
    private NodeContext context;

    /**
     * 启动标识
     */
    private boolean started;

    /**
     * 当前节点的角色信息
     */
    private AbstractNodeRole role;

    public NodeImpl(NodeContext context) {
        this.context = context;
    }

    @Override
    public void start() {
        if (started) {
            return;
        }

        // 注册消息总线
        context.getEventBus().register(this);
        // 初始化Connector
        context.getConnector().initialize();
        // 设置角色为Follower
        NodeStore store = context.getStore();
        changeToRole(new Follower(store.getTerm(), store.getVotedFor(), null, scheduleElectionTimeout()));
        started = true;
    }

    @Override
    public void stop() throws InterruptedException {
        if (!started) {
            throw new IllegalStateException("");
        }

        // 关闭调度器
        context.getScheduler().stop();
        // 关闭Connector
        context.getConnector().close();
        // 关闭主线程
        context.getTaskExecutor().shutdown();
        started = false;
    }


    public void onReceiveRequestVoteRpc(RequestVoteRpcMsg msg) {

    }


    private void changeToRole(AbstractNodeRole newRole) {
        NodeStore store = context.getStore();
        store.setTerm(newRole.getTerm());
        if (newRole.getName() == RoleName.FOLLOWER) {
            store.setVotedFor(((Follower) newRole).getVoteFor());
        }
        role = newRole;
    }

    private ElectionTimeout scheduleElectionTimeout() {
        return context.getScheduler().scheduleElectionTimeout(this::electionTimeout);
    }

    private void electionTimeout() {
        context.getTaskExecutor().submit(this::doProcessElectionTimeout);
    }

    private void doProcessElectionTimeout() {
        // leader节点不存在选举超时
        if (role.getName() == RoleName.LEADER) {
            // TODO 补充
            log.warn("");
            return;
        }

        // to Follower，发起选举
        // to Candidate，再次发起选举
        // 增加term
        int newTerm = role.getTerm() + 1;
        // 取消选举超时定时任务 / 心跳定时任务
        role.cancelTimeoutOrTask();
        // 变更角色为Candiate
        changeToRole(new Candidate(newTerm, scheduleElectionTimeout()));

        // 发送RequestVote消息
        RequestVoteRpc rpc = RequestVoteRpc.builder()
                .term(newTerm)
                .candidateId(context.getSelfId())
                .lastLogIndex(0)
                .lastLogTerm(0)
                .build();
        context.getConnector().sendRequestVote(rpc, context.getGroup().listEndpointExceptSelf());
    }
}
