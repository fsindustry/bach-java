package com.fsindustry.bach.core.log.sequence;

import com.fsindustry.bach.core.log.entry.EntryMeta;
import com.fsindustry.bach.core.log.entry.LogEntry;
import com.fsindustry.bach.core.log.exception.EmptySequenceException;
import lombok.ToString;

import java.util.Collections;
import java.util.List;

/**
 * 日志序列公共部分抽取
 */
@ToString
public abstract class AbstractEntrySequence implements EntrySequence {

    // 记录已读取offset的位置
    int logIndexOffset;

    // 记录带写入offset的位置
    int nextLogIndex;

    public AbstractEntrySequence(int logIndexOffset) {
        this.logIndexOffset = logIndexOffset;
        this.nextLogIndex = nextLogIndex;
    }

    @Override
    public boolean isEmpty() {
        return logIndexOffset == nextLogIndex;
    }

    @Override
    public int getFirstLogIndex() {
        if (isEmpty()) {
            throw new EmptySequenceException();
        }
        return doGetFirstLogIndex();
    }

    int doGetFirstLogIndex() {
        return logIndexOffset;
    }

    @Override
    public int getLastLogIndex() {
        if (isEmpty()) {
            throw new EmptySequenceException();
        }

        return doGetLastLogIndex();
    }

    int doGetLastLogIndex() {
        return nextLogIndex - 1;
    }

    @Override
    public int getNextLogIndex() {
        return nextLogIndex;
    }

    @Override
    public List<LogEntry> subList(int fromIndex) {
        if (isEmpty() || fromIndex > doGetLastLogIndex()) {
            return Collections.emptyList();
        }
        return subList(Math.max(fromIndex, doGetFirstLogIndex()), nextLogIndex);
    }

    @Override
    public List<LogEntry> subList(int fromIndex, int toIndex) {
        if (isEmpty()) {
            throw new EmptySequenceException();
        }

        if (fromIndex < doGetFirstLogIndex() ||
                toIndex > doGetLastLogIndex() + 1 ||
                fromIndex > toIndex) {
            throw new IllegalArgumentException(String.format("invalid log sequence range [ %d, %d )", fromIndex, toIndex));
        }

        return doSubList(fromIndex, toIndex);
    }

    @Override
    public boolean isEntryPresent(int index) {
        // 根据日志区间范围判断日志是否存在
        return !isEmpty() && index >= doGetFirstLogIndex() && index <= doGetLastLogIndex();
    }

    @Override
    public EntryMeta getEntryMeta(int index) {
        LogEntry entry = getEntry(index);
        return entry == null ? null : entry.getMeta();
    }

    @Override
    public LogEntry getEntry(int index) {
        if (!isEntryPresent(index)) {
            return null;
        }
        return doGetEntry(index);
    }

    @Override
    public LogEntry getLastEntry() {
        return isEmpty() ? null : doGetEntry(doGetLastLogIndex());
    }

    @Override
    public void append(LogEntry entry) {
        if (entry.getIndex() != nextLogIndex) {
            throw new IllegalArgumentException(String.format("unmatched log index, need: %d, got: %d", nextLogIndex, entry.getIndex()));
        }
        doAppend(entry);
        // 递增序列的日志索引
        nextLogIndex++;
    }


    @Override
    public void append(List<LogEntry> entries) {
        for (LogEntry entry : entries) {
            append(entry);
        }
    }

    @Override
    public void removeAfter(int index) {
        if (isEmpty() || index >= doGetLastLogIndex()) {
            return;
        }

        doRemoveAfter(index);
    }

    protected abstract List<LogEntry> doSubList(int fromIndex, int toIndex);

    protected abstract LogEntry doGetEntry(int index);

    protected abstract void doAppend(LogEntry entry);

    protected abstract void doRemoveAfter(int index);
}
