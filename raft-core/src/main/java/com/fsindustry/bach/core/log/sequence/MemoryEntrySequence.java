package com.fsindustry.bach.core.log.sequence;

import com.fsindustry.bach.core.log.entry.LogEntry;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

/**
 * 基于内存实现EntrySequence（测试用）
 */
@ToString(callSuper = true)
public class MemoryEntrySequence extends AbstractEntrySequence {

    private final List<LogEntry> entries = new ArrayList<>();

    private int commitIndex = 0;

    public MemoryEntrySequence() {
        this(1);
    }

    public MemoryEntrySequence(int logIndexOffset) {
        super(logIndexOffset);
    }

    @Override
    protected List<LogEntry> doSubList(int fromIndex, int toIndex) {
        return entries.subList(fromIndex - logIndexOffset, toIndex - logIndexOffset);
    }

    @Override
    protected LogEntry doGetEntry(int index) {
        return entries.get(index - logIndexOffset);
    }

    @Override
    protected void doAppend(LogEntry entry) {
        entries.add(entry);
    }

    @Override
    protected void doRemoveAfter(int index) {
        if (index < doGetFirstLogIndex()) {
            entries.clear();
            nextLogIndex = logIndexOffset;
        } else {
            entries.subList(index - logIndexOffset + 1, entries.size()).clear();
            nextLogIndex = index + 1;
        }
    }

    @Override
    public void commit(int index) {
        commitIndex = index;
    }

    @Override
    public int getCommitIndex() {
        return commitIndex;
    }

    @Override
    public void close() {
    }
}
