package com.fsindustry.bach.core.log.entry;

/**
 * 定义一个日志条目
 */
public interface LogEntry {

    /**
     * 获取日志类型
     */
    int getType();

    /**
     * 获取日志索引
     */
    int getIndex();

    /**
     * 获取日志term
     */
    int getTerm();

    /**
     * 获取日志元信息（type, index, term）
     */
    EntryMeta getMeta();

    /**
     * 获取日志payload
     */
    byte[] getPayload();
}
