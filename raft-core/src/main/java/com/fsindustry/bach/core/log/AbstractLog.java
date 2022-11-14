package com.fsindustry.bach.core.log;

import com.fsindustry.bach.core.connector.msg.vo.AppendEntriesRpc;
import com.fsindustry.bach.core.log.entry.*;
import com.fsindustry.bach.core.log.sequence.EntrySequence;
import com.fsindustry.bach.core.log.statemachine.StateMachine;
import com.fsindustry.bach.core.node.model.NodeId;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

@Slf4j
public abstract class AbstractLog implements Log {

    protected EntrySequence sequence;
    protected int commitIndex = 0;

    /**
     * 日志状态机
     */
    @Setter
    private StateMachine stateMachine;

    public EntryMeta getLastEntryMeta() {
        if (sequence.isEmpty()) {
            return new EntryMeta(LogType.noop.getType(), 0, 0);
        }
        return sequence.getLastEntry().getMeta();
    }

    @Override
    public AppendEntriesRpc createAppendEntriesRpc(int term, NodeId selfId, int nextIndex, int maxEntries) {
        int nextLogIndex = sequence.getNextLogIndex();
        if (nextIndex > nextLogIndex) {
            log.error("illegal next index: {} which is larger than expected: {}", nextIndex, nextLogIndex);
            throw new IllegalArgumentException(String.format("illegal next index: %s which is larger than expected: %s", nextIndex, nextLogIndex));
        }

        AppendEntriesRpc rpc = new AppendEntriesRpc();
        rpc.setTerm(term);
        rpc.setLeaderId(selfId);
        rpc.setLeaderCommit(commitIndex);
        LogEntry entry = sequence.getEntry(nextIndex - 1);
        if (entry != null) {
            rpc.setPrevLogIndex(entry.getIndex());
            rpc.setPrevLogTerm(entry.getTerm());
        }
        if (!sequence.isEmpty()) {
            int maxIndex = (maxEntries == ALL_ENTRIES) ? nextLogIndex : Math.min(nextLogIndex, nextIndex + maxEntries);
            rpc.setEntries(sequence.subList(nextIndex, maxIndex));
        }
        return rpc;
    }

    @Override
    public boolean isNewerThan(int lastLogIndex, int lastLogTerm) {
        EntryMeta lastEntryMeta = getLastEntryMeta();
        log.debug("last entry ({}, {}), candidate ({}, {})", lastEntryMeta.getIndex(), lastEntryMeta.getTerm(), lastLogIndex, lastLogTerm);
        return lastEntryMeta.getTerm() > lastLogTerm || lastEntryMeta.getIndex() > lastLogIndex;
    }

    @Override
    public NoopEntry appendEntry(int term) {
        NoopEntry entry = new NoopEntry(sequence.getNextLogIndex(), term);
        sequence.append(entry);
        return entry;
    }

    @Override
    public GeneralEntry appendEntry(int term, byte[] payload) {
        GeneralEntry entry = new GeneralEntry(sequence.getNextLogIndex(), term, payload);
        sequence.append(entry);
        return entry;
    }

    @Override
    public boolean appendEntriesFromLeader(int prevLogIndex, int prevLogTerm, List<LogEntry> entries) {
        // check previous log
        if (!checkIfPreviousLogMatches(prevLogIndex, prevLogTerm)) {
            return false;
        }

        // heartbeat
        if (entries.isEmpty()) {
            return true;
        }

        assert prevLogIndex + 1 == entries.get(0).getIndex();
        // 注意：不一定从日志末尾开始追加，故此处有可能要覆盖本地日志
        EntrySequenceView newEntries = removeUnmatchedLog(new EntrySequenceView(entries));
        // 从不匹配位置开始，覆盖追加leader日志到本地文件
        appendEntriesFromLeader(newEntries);
        return true;
    }


    @Override
    public void advanceCommitIndex(int newCommitIndex, int currentTerm) {
        if (!validateNewCommitIndex(newCommitIndex, currentTerm)) {
            return;
        }
        log.debug("advance commit index from {} to {}", commitIndex, newCommitIndex);
        sequence.commit(newCommitIndex);
        commitIndex = newCommitIndex;
    }

    @Override
    public int getNextIndex() {
        return sequence.getNextLogIndex();
    }

    @Override
    public int getCommitIndex() {
        return commitIndex;
    }

    @Override
    public void close() {
        sequence.close();
    }

    private boolean validateNewCommitIndex(int newCommitIndex, int currentTerm) {
        if (newCommitIndex <= commitIndex) {
            return false;
        }
        LogEntry entry = sequence.getEntry(newCommitIndex);
        if (entry == null) {
            log.debug("log of new commit index {} not found", newCommitIndex);
            return false;
        }
        // 提交日志的前提，是term必须一致
        if (entry.getTerm() != currentTerm) {
            log.debug("log term of new commit index != current term ({} != {})", entry.getTerm(), currentTerm);
            return false;
        }
        return true;
    }

    private void appendEntriesFromLeader(EntrySequenceView logEntries) {
        if (logEntries.isEmpty()) {
            return;
        }
        log.debug("append entries from leader from {} to {}", logEntries.getFirstLogIndex(), logEntries.getLastLogIndex());
        for (LogEntry leaderEntry : logEntries) {
            sequence.append(leaderEntry);
        }
    }

    private EntrySequenceView removeUnmatchedLog(EntrySequenceView logEntries) {
        assert !logEntries.isEmpty();
        int firstUnmatched = findFirstUnmatchedLog(logEntries);
        // 删除本地log文件第一条不匹配的日志条目之后的日志
        removeEntriesAfter(firstUnmatched - 1);
        // 从不匹配日志位置开始截取保留之后的日志
        return logEntries.subView(firstUnmatched);
    }

    private void removeEntriesAfter(int index) {
        if (sequence.isEmpty() || index >= sequence.getLastLogIndex()) {
            return;
        }

        // todo 重建状态机
        log.debug("remove entries after {}", index);
        sequence.removeAfter(index);
    }

    private int findFirstUnmatchedLog(EntrySequenceView logEntries) {
        assert !logEntries.isEmpty();
        int logIndex;
        EntryMeta followerEntryMeta;
        for (LogEntry leaderEntry : logEntries) {
            logIndex = leaderEntry.getIndex();
            followerEntryMeta = sequence.getEntryMeta(logIndex);
            if (followerEntryMeta == null || followerEntryMeta.getTerm() != leaderEntry.getTerm()) {
                return logIndex;
            }
        }
        // 未找到不匹配日志，则返回下一条日志索引
        return logEntries.getLastLogIndex() + 1;
    }

    /**
     * 检查指定日志的index和term是否和本地一致
     * todo 为了保证数据一致，是否还应该增加校验和
     */
    private boolean checkIfPreviousLogMatches(int prevLogIndex, int prevLogTerm) {
        LogEntry entry = sequence.getEntry(prevLogIndex);
        if (entry == null) {
            log.debug("previous log {} not found", prevLogIndex);
            return false;
        }
        int term = entry.getTerm();
        if (term != prevLogTerm) {
            log.debug("different term of previous log, local {}, remote {}", term, prevLogTerm);
            return false;
        }
        return true;
    }

    /**
     * 保存待追加的日志序列的切片
     */
    private static class EntrySequenceView implements Iterable<LogEntry> {

        /**
         * 切片中包含的日志
         */
        private final List<LogEntry> entries;

        /**
         * 切片的起始日志索引
         */
        private int firstLogIndex;

        /**
         * 切片的最后一条日志索引
         */
        private int lastLogIndex;

        EntrySequenceView(List<LogEntry> entries) {
            this.entries = entries;
            if (!entries.isEmpty()) {
                firstLogIndex = entries.get(0).getIndex();
                lastLogIndex = entries.get(entries.size() - 1).getIndex();
            }
        }

        LogEntry get(int index) {
            if (entries.isEmpty() || index < firstLogIndex || index > lastLogIndex) {
                return null;
            }
            return entries.get(index - firstLogIndex);
        }

        boolean isEmpty() {
            return entries.isEmpty();
        }

        int getFirstLogIndex() {
            return firstLogIndex;
        }

        int getLastLogIndex() {
            return lastLogIndex;
        }

        EntrySequenceView subView(int fromIndex) {
            if (entries.isEmpty() || fromIndex > lastLogIndex) {
                return new EntrySequenceView(Collections.emptyList());
            }
            return new EntrySequenceView(
                    entries.subList(fromIndex - firstLogIndex, entries.size())
            );
        }

        @Override
        @Nonnull
        public Iterator<LogEntry> iterator() {
            return entries.iterator();
        }
    }
}
