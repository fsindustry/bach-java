package com.fsindustry.bach.core.connector.model;

import lombok.Value;

/**
 * 选举RPC响应
 */
@Value
public class RequestVoteResult {

    /**
     * 选举term
     */
    private final int term;

    /**
     * 是否投票
     */
    private final boolean voteGranted;
}
