package com.fsindustry.bach.core.node;

import com.fsindustry.bach.core.NodeContext;
import com.fsindustry.bach.core.connector.msg.AppendEntriesResultMsg;
import com.fsindustry.bach.core.connector.msg.AppendEntriesRpcMsg;
import com.fsindustry.bach.core.connector.msg.RequestVoteRpcMsg;
import com.fsindustry.bach.core.connector.msg.vo.AppendEntriesResult;
import com.fsindustry.bach.core.connector.msg.vo.AppendEntriesRpc;
import com.fsindustry.bach.core.connector.msg.vo.RequestVoteResult;
import com.fsindustry.bach.core.connector.msg.vo.RequestVoteRpc;
import com.fsindustry.bach.core.log.entry.EntryMeta;
import com.fsindustry.bach.core.log.statemachine.StateMachine;
import com.fsindustry.bach.core.node.exception.NotLeaderException;
import com.fsindustry.bach.core.node.model.GroupMember;
import com.fsindustry.bach.core.node.model.NodeEndpoint;
import com.fsindustry.bach.core.node.model.NodeId;
import com.fsindustry.bach.core.node.role.*;
import com.fsindustry.bach.core.node.store.NodeStore;
import com.fsindustry.bach.core.schedule.ElectionTimeout;
import com.fsindustry.bach.core.schedule.LogReplicationTask;
import com.google.common.eventbus.Subscribe;
import com.google.common.util.concurrent.FutureCallback;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

@Slf4j
public class NodeImpl implements Node {

    /**
     * 节点状态上下文
     */
    private final NodeContext context;

    /**
     * 当前节点是否启动
     */
    private volatile boolean started;

    /**
     * 当前节点的角色信息
     */
    private AbstractNodeRole role;

    public NodeImpl(NodeContext context) {
        this.context = context;
    }

    /**
     * 核心线程调度任务异常回调
     */
    private static final FutureCallback<Object> LOGGING_FUTURE_CALLBACK = new FutureCallback<Object>() {
        @Override
        public void onSuccess(@Nullable Object result) {
        }

        @Override
        public void onFailure(@Nonnull Throwable t) {
            log.warn("failure", t);
        }
    };


    @Override
    public void registerStateMachine(StateMachine stateMachine) {
        context.getLog().setStateMachine(stateMachine);
    }

    @Override
    public RoleNameAndLeaderId getRoleNameAndLeaderId() {
        return role.getNameAndLeaderId(context.getSelfId());
    }

    @Override
    public void start() {
        if (started) {
            return;
        }

        // 注册消息总线，以便能够监听各类事件
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
            log.error("node {} was not started.", context.getSelfId());
            throw new IllegalStateException(String.format("node %s was not started.", context.getSelfId()));
        }

        // 关闭调度器（从而停止产生消息）
        context.getScheduler().stop();
        // 关闭Connector（从而关闭网络）
        context.getConnector().close();
        // 关闭主线程（关闭状态机运行）
        context.getTaskExecutor().shutdown();
        started = false;
    }

    /**
     * RequestVoteRpc消息处理函数
     */
    @Override
    @Subscribe
    public void onReceiveRequestVoteRpc(RequestVoteRpcMsg msg) {
        // 提交消息到任务队列执行
        context.getTaskExecutor().submit(() -> context.getConnector().replyRequestVote(
                doProcessRequestVoteRpc(msg),
                msg
        ));
    }

    private RequestVoteResult doProcessRequestVoteRpc(RequestVoteRpcMsg msg) {
        RequestVoteRpc rpc = msg.getRpc();

        // 场景1：若rpc.term < role.term，则不投票，直接返回role.term
        if (rpc.getTerm() < role.getTerm()) {
            return new RequestVoteResult(role.getTerm(), false);
        }

        // 判断竞选的Candidate的日志是否比当前节点新
        boolean voteForCandidate = !context.getLog().isNewerThan(rpc.getLastLogIndex(), rpc.getLastLogTerm());

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
                log.error("unknow status: {}", role.getName());
                throw new IllegalStateException(String.format("unknow status: %s", role.getName()));
            }
        }
    }

    /**
     * RequestVoteResult消息处理函数
     */
    @Override
    @Subscribe
    public void onReceiveRequestVoteResult(RequestVoteResult result) {
        context.getTaskExecutor().submit(() -> doProcessRequestVoteResult(result));
    }

    private void doProcessRequestVoteResult(RequestVoteResult result) {
        // 场景1：若result.getTerm() > role.getTerm()，则当前节点退化为Follower
        if (result.getTerm() > role.getTerm()) {
            // todo leaderId从何而来？
            becomeFollower(result.getTerm(), null, null, true);
            return;
        }

        // result.getTerm() == role.getTerm()时，仅当当前节点仍然是CANDIDATE，才处理选举结果，否则忽略
        if (!RoleName.CANDIDATE.equals(role.getName())) {
            log.warn("current node is not candidate, ignore.");
            return;
        }

        // 若result.getTerm() < role.getTerm()或者投了反对票，则忽略
        if (result.getTerm() < role.getTerm() || !result.isVoteGranted()) {
            log.info("unmatched vote, term: {}, voteGranted: {}", result.getTerm(), result.isVoteGranted());
            return;
        }

        // 已获得的选票数
        int votesCount = ((Candidate) role).getVotesCount() + 1;
        // 已投票的节点数
        int nodeCount = context.getGroup().getCount();

        log.info("votes count {}, node count {}", votesCount, nodeCount);

        // 取消选举定时器
        role.cancelTimeoutOrTask();
        if (votesCount > nodeCount / 2) { // 若投票过半，则变为Leader
            log.info("candidate become leader, term: {}", role.getTerm());
            // 创建日志追加定时任务，变更角色为leader
            changeToRole(new Leader(role.getTerm(), scheduleLogReplicationTask()));
            // todo 追加空日志到本地
        } else { // 若投票未过半，则重置定时器，重新发起选举
            changeToRole(new Candidate(role.getTerm(), votesCount, scheduleElectionTimeout()));
        }
    }

    /**
     * 创建并启动日志追加定时任务
     */
    private LogReplicationTask scheduleLogReplicationTask() {
        return context.getScheduler().scheduleLogReplicationTask(this::replicateLog);
    }

    /**
     * 日志追加定时任务入口方法
     */
    private void replicateLog() {
        context.getTaskExecutor().submit(this::doReplicateLog);
    }

    private void doReplicateLog() {
        log.debug("start to replicate log.");
        // 发送AppendEntriesRpc
        for (GroupMember member : context.getGroup().listReplicationTarget()) {
            AppendEntriesRpc rpc = context.getLog().createAppendEntriesRpc(role.getTerm(), context.getSelfId(), member.getNextIndex(), context.getConfig().getMaxReplicationEntries());
            context.getConnector().sendAppendEntries(rpc, member.getEndpoint());
        }
    }

    private void becomeFollower(int term, NodeId votedFor, NodeId leaderId, boolean scheduleElectionTimeout) {
        // 取消选举超时 / 心跳超时定时器
        role.cancelTimeoutOrTask();
        if (leaderId != null && !leaderId.equals(role.getLeaderId(context.getSelfId()))) {
            log.info("current leader is {}, term {}", leaderId, term);
        }

        // 重置选举超时定时任务
        ElectionTimeout electionTimeout = scheduleElectionTimeout ? scheduleElectionTimeout() : ElectionTimeout.NONE;
        changeToRole(new Follower(term, votedFor, leaderId, electionTimeout));
    }


    private void changeToRole(AbstractNodeRole newRole) {
        log.info("node {} , role state changed -> {}", context.getSelfId(), newRole);
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

    /**
     * 选举超时触发动作入口
     */
    private void electionTimeout() {
        context.getTaskExecutor().submit(this::doProcessElectionTimeout);
    }

    private void doProcessElectionTimeout() {
        // 边界检查：leader节点不存在选举超时
        if (role.getName() == RoleName.LEADER) {
            log.warn("node {}, current role is leader, ignore election timeout.", context.getSelfId());
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
        // 获取最后一条日志元数据
        EntryMeta last = context.getLog().getLastEntryMeta();

        // 发送RequestVote消息
        RequestVoteRpc rpc = RequestVoteRpc.builder()
                .term(newTerm)
                .candidateId(context.getSelfId())
                .lastLogIndex(last.getIndex())
                .lastLogTerm(last.getTerm())
                .build();
        context.getConnector().sendRequestVote(rpc, context.getGroup().listEndpointExceptSelf());
    }

    @Override
    @Subscribe
    public void onReceiveAppendEntriesRpc(AppendEntriesRpcMsg msg) {
        context.getTaskExecutor().submit(() ->
                context.getConnector().replyAppendEntries(
                        doProcessAppendEntriesRpc(msg),
                        msg)
        );
    }

    private AppendEntriesResult doProcessAppendEntriesRpc(AppendEntriesRpcMsg msg) {

        AppendEntriesRpc rpc = msg.getRpc();
        // 场景1：若收到的term比自己的term小，则返回自己的term
        if (rpc.getTerm() < role.getTerm()) {
            return new AppendEntriesResult(role.getTerm(), false);
        }

        // 场景2:若收到term比自己term大，则退化为发送消息Node的follower
        if (rpc.getTerm() > role.getTerm()) {
            becomeFollower(rpc.getTerm(), null, rpc.getLeaderId(), true);
            // 追加日志，并返回结果
            return new AppendEntriesResult(rpc.getTerm(), appendEntries(rpc));
        }

        // 场景3：若收到term等于自己term
        assert (rpc.getTerm() == role.getTerm());
        switch (role.getName()) {
            // 若当前节点为follower，则重置选举超时定时器，追加日志
            case FOLLOWER: {
                // 重置选举定时器，保存leader信息
                // todo 为什么此处需要传入votedFor字段
                becomeFollower(rpc.getTerm(), ((Follower) role).getVoteFor(), rpc.getLeaderId(), true);
                // 追加日志，并返回结果
                return new AppendEntriesResult(rpc.getTerm(), appendEntries(rpc));
            }
            // 若当前节点为candidate，则重置选举定时器，追加日志
            case CANDIDATE: {
                // 重置选举定时器，保存leader信息
                // todo 为什么此处需要传入votedFor字段
                becomeFollower(rpc.getTerm(), null, rpc.getLeaderId(), true);
                // 追加日志，并返回结果
                return new AppendEntriesResult(rpc.getTerm(), appendEntries(rpc));
            }
            // 若当前节点为leader，则为垃圾消息，直接忽略
            case LEADER: {
                log.warn("receive append entries rpc from another leader {}, ignore.", rpc.getLeaderId());
                return new AppendEntriesResult(rpc.getTerm(), false);
            }
            default: {
                log.error("unknown role name: {}", role.getName());
                throw new IllegalStateException(String.format("unknown role name: %s", role.getName()));
            }
        }
    }

    private boolean appendEntries(AppendEntriesRpc rpc) {
        // 追加当前日志
        boolean result = context.getLog().appendEntriesFromLeader(rpc.getPrevLogIndex(), rpc.getPrevLogTerm(), rpc.getEntries());
        if (result) {
            // 提交上一次追加日志，为什么要用 min( commitIndex, lastIndex )
            context.getLog().advanceCommitIndex(Math.min(rpc.getLeaderCommit(), rpc.getLastEntryIndex()), rpc.getTerm());
        }
        return result;
    }

    @Override
    @Subscribe
    public void onReceiveAppendEntriesResult(AppendEntriesResultMsg msg) {
        context.getTaskExecutor().submit(() -> doProcessAppendEntriesResult(msg));
    }


    private void doProcessAppendEntriesResult(AppendEntriesResultMsg msg) {
        AppendEntriesResult result = msg.getResult();

        // 若消息term大于当前term，则退化为follower
        if (result.getTerm() > role.getTerm()) {
            becomeFollower(result.getTerm(), null, null, true);
            return;
        }

        if (!RoleName.LEADER.equals(role.getName())) {
            log.warn("receive appendEntries result, but current node is not leader, ignore. source: {}", msg.getSourceNodeId());
        }

        // 更新日志提交进度
        NodeId sourceNodeId = msg.getSourceNodeId();
        GroupMember member = context.getGroup().getMember(sourceNodeId);
        if (member == null) {
            log.warn("unexpected nodeId: {}, node maybe removed.", sourceNodeId);
            return;
        }

        AppendEntriesRpc rpc = msg.getRpc();
        // 若追加日志成功
        if (result.isSuccess()) {
            // 若追加日志成功， 更新本地日志的提交和追加进度
            if (member.advanceReplicatingState(rpc.getLastEntryIndex())) {
                // 计算提交过半的commitIndex，并提交日志
                // 计算方法：对所有node根据matchIndex排序，取中间节点的matchIndex，即为提交过半的commitIndex
                context.getLog().advanceCommitIndex(context.getGroup().getMatchIndexOfMajor(), role.getTerm());
            }
        }
        // 若追加日志失败
        else {
            // 回退追加日志索引
            // todo 回退后，下一次日志追加消息什么地方发送？
            if (member.backOffNextIndex()) {
                log.warn("can't back off next index any more, node: {}", sourceNodeId);
            }
        }
    }

    @Override
    public void appendLog(byte[] commandBytes) {
        ensureLeader();
        context.getTaskExecutor().submit(() -> {
            // 追加日志
            context.getLog().appendEntry(role.getTerm(), commandBytes);
            // 发送appendEntriesRpc给其它节点
            doReplicateLog();
        }, LOGGING_FUTURE_CALLBACK);
    }

    /**
     * Ensure leader status
     */
    private void ensureLeader() {
        RoleNameAndLeaderId result = role.getNameAndLeaderId(context.getSelfId());
        if (result.getRoleName() == RoleName.LEADER) {
            return;
        }
        // 若不是leader，则报错
        NodeEndpoint endpoint = result.getLeaderId() != null ? context.getGroup().findMember(result.getLeaderId()).getEndpoint() : null;
        throw new NotLeaderException(result.getRoleName(), endpoint);
    }
}
