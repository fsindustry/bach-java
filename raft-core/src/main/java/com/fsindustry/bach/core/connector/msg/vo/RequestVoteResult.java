package com.fsindustry.bach.core.connector.msg.vo;

import lombok.Value;

/**
 * 选举RPC响应
 */
@Value
public class RequestVoteResult {

    /**
     * 选举term
     */
    int term;

    /**
     * 是否投票
     */
    boolean voteGranted;
}
