package com.fsindustry.bach.core.connector.msg.vo;

import lombok.Value;

/**
 * 选举RPC响应
 */
@Value
public class AppendEntriesResult {

    /**
     * 选举term
     */
    private final int term;

    /**
     * 是否追加成功
     */
    private final boolean success;
}
