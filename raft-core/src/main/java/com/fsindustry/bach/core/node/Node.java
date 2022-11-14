package com.fsindustry.bach.core.node;

import com.fsindustry.bach.core.connector.msg.AppendEntriesResultMsg;
import com.fsindustry.bach.core.connector.msg.AppendEntriesRpcMsg;
import com.fsindustry.bach.core.connector.msg.RequestVoteRpcMsg;
import com.fsindustry.bach.core.connector.msg.vo.RequestVoteResult;
import com.fsindustry.bach.core.log.statemachine.StateMachine;
import com.fsindustry.bach.core.node.role.RoleNameAndLeaderId;

/**
 * raft状态机的核心实现
 */
public interface Node {

    /**
     * 注册状态机
     */
    void registerStateMachine(StateMachine stateMachine);

    /**
     * 获取当前Node的角色以及对应的LeaderId
     */
    RoleNameAndLeaderId getRoleNameAndLeaderId();

    /**
     * 启动
     */
    void start();

    /**
     * 停止
     */
    void stop() throws InterruptedException;

    /**
     * 收到选举请求的处理动作
     */
    void onReceiveRequestVoteRpc(RequestVoteRpcMsg msg);

    /**
     * 收到选举响应的处理动作
     */
    void onReceiveRequestVoteResult(RequestVoteResult result);

    /**
     * 收到追加日志请求的处理动作
     */
    void onReceiveAppendEntriesRpc(AppendEntriesRpcMsg msg);

    /**
     * 收到追加日志响应的处理动作
     */
    void onReceiveAppendEntriesResult(AppendEntriesResultMsg msg);

    /**
     * 追加日志（业务调用入口）
     */
    void appendLog(byte[] commandBytes);
}
