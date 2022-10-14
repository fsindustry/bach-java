package com.fsindustry.bach.core.log.sequence;

import com.fsindustry.bach.core.log.entry.EntryMeta;
import com.fsindustry.bach.core.log.entry.LogEntry;

import java.util.List;

/**
 * 日志存储操作接口
 * 日志的stream或者缓冲区
 */
public interface EntrySequence {

    /**
     * 判断是否为空
     */
    boolean isEmpty();

    /**
     * 获取第一条日志的索引
     */
    int getFirstLogIndex();

    /**
     * 获取最后一条日志的索引
     */
    int getLastLogIndex();

    /**
     * 获取下一条日志的索引
     */
    int getNextLogIndex();

    /**
     * 获取指定索引到最后的日志序列
     */
    List<LogEntry> subList(int fromIndex);

    /**
     * 获取指定索引范围的日志序列，区间 [fromIndex, toIndex)
     */
    List<LogEntry> subList(int fromIndex, int toIndex);

    /**
     * 检查指定索引日志是否存在
     */
    boolean isEntryPresent(int index);

    /**
     * 获取指定索引日志的元信息
     */
    EntryMeta getEntryMeta(int index);

    /**
     * 获取指定索引的日志
     */
    LogEntry getEntry(int index);

    /**
     * 获取最后一条日志
     */
    LogEntry getLastEntry();

    /**
     * 追加日志
     */
    void append(LogEntry entry);

    /**
     * 追加多条日志
     */
    void append(List<LogEntry> entries);

    /**
     * 提交日志，推进commitIndex
     */
    void commit(int index);

    /**
     * 获取当前的commitIndex
     */
    int getCommitIndex();

    /**
     * 删除指定索引之后的日志
     */
    void removeAfter(int index);

    /**
     * 关闭日志序列
     */
    void close();
}
