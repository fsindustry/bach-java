package com.fsindustry.bach.core.node;

import com.fsindustry.bach.core.connector.msg.AppendEntriesResultMsg;
import com.fsindustry.bach.core.connector.msg.AppendEntriesRpcMsg;
import com.fsindustry.bach.core.connector.msg.RequestVoteRpcMsg;
import com.fsindustry.bach.core.connector.msg.vo.RequestVoteResult;

/**
 * raft状态机的核心实现
 */
public interface Node {

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
}
