package com.fsindustry.bach.core.node;

import com.fsindustry.bach.core.NodeContext;
import com.fsindustry.bach.core.connector.msg.RequestVoteRpcMsg;
import com.fsindustry.bach.core.connector.msg.vo.RequestVoteResult;
import com.fsindustry.bach.core.connector.msg.vo.RequestVoteRpc;
import com.fsindustry.bach.core.node.model.NodeId;
import com.fsindustry.bach.core.node.role.AbstractNodeRole;
import com.fsindustry.bach.core.node.role.Candidate;
import com.fsindustry.bach.core.node.role.Follower;
import com.fsindustry.bach.core.node.role.RoleName;
import com.fsindustry.bach.core.node.store.NodeStore;
import com.fsindustry.bach.core.schedule.ElectionTimeout;
import com.google.common.eventbus.Subscribe;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

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

    /**
     * RequestVoteRpc消息处理函数
     */
    @Subscribe
    public void onReceiveRequestVoteRpc(RequestVoteRpcMsg msg) {
        // 提交消息到任务队列执行
        context.getTaskExecutor().submit(() -> context.getConnector().replyRequestVote(
                doProcessRequestVoteRpc(msg),
                msg
        ));
    }

    /**
     * RequestVoteResult消息处理函数
     */
    @Subscribe
    public void onReceiveRequestVoteResult(RequestVoteResult result) {
        context.getTaskExecutor().submit(() -> doProcessRequestVoteResult(result));
    }

    private void doProcessRequestVoteResult(RequestVoteResult result) {

    }

    private RequestVoteResult doProcessRequestVoteRpc(RequestVoteRpcMsg msg) {
        RequestVoteRpc rpc = msg.getRpc();

        // 场景1：若rpc.term < role.term，则不投票，直接返回role.term
        if (rpc.getTerm() < role.getTerm()) {
            return new RequestVoteResult(role.getTerm(), false);
        }

        // todo 实现投票策略
        boolean voteForCandidate = true;

        // 场景2：若rpc.term > role.term，则变为follower角色
        if (rpc.getTerm() > role.getTerm()) {
            becomeFollower(rpc.getTerm(), voteForCandidate ? rpc.getCandidateId() : null, null, true);
            return new RequestVoteResult(rpc.getTerm(), voteForCandidate);
        }

        // 场景3：若rpc.term == role.term，则根据角色的不同情况不同
        switch (role.getName()) {
            case FOLLOWER:
                Follower follower = (Follower) role;
                NodeId votedFor = follower.getVoteFor();
                // 若未投票，或者已经投票过相同的节点
                if ((null == votedFor && voteForCandidate) ||
                        Objects.equals(votedFor, rpc.getCandidateId())) {
                    becomeFollower(rpc.getTerm(), rpc.getCandidateId(), null, true);
                    return new RequestVoteResult(rpc.getTerm(), true);
                }
                // 若已投票，且是其它节点发来的请求，则不投票
                else {
                    return new RequestVoteResult(rpc.getTerm(), false);
                }
            case LEADER: // leader不投票
            case CANDIDATE: { // candidate已给自己投票，故都不投票
                return new RequestVoteResult(rpc.getTerm(), false);
            }
            default: {
                // todo 实现
                log.error("");
                throw new IllegalStateException("");
            }
        }
    }

    private void becomeFollower(int term, NodeId votedFor, NodeId leaderId, boolean scheduleElectionTimeout) {
        // 取消选举超时 / 心跳超时定时器
        role.cancelTimeoutOrTask();
        if (leaderId != null && !leaderId.equals(role.getLeaderId(context.getSelfId()))) {
            // todo 补充
            log.info("");
        }

        ElectionTimeout electionTimeout = scheduleElectionTimeout ? scheduleElectionTimeout() : ElectionTimeout.NONE;
        changeToRole(new Follower(term, votedFor, leaderId, electionTimeout));
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
