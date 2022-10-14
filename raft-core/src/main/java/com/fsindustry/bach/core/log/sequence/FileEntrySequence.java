package com.fsindustry.bach.core.log.sequence;

import com.fsindustry.bach.core.log.entry.EntryFactory;
import com.fsindustry.bach.core.log.entry.EntryMeta;
import com.fsindustry.bach.core.log.entry.LogEntry;
import com.fsindustry.bach.core.log.exception.LogException;
import com.fsindustry.bach.core.log.file.EntryFile;
import com.fsindustry.bach.core.log.file.EntryIndexFile;
import com.fsindustry.bach.core.log.file.LogDir;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

@Slf4j
@ToString(callSuper = true)
public class FileEntrySequence extends AbstractEntrySequence {

    private final EntryFactory entryFactory = new EntryFactory();
    private final EntryFile entryFile;
    private final EntryIndexFile indexFile;
    private final LinkedList<LogEntry> pendingEntries = new LinkedList<>();
    // raft算法中，初始commitIndex = 0，和日志是否持久化无关
    private int commitIndex = 0;

    public FileEntrySequence(LogDir logDir, int logIndexOffset) {
        super(logIndexOffset);
        try {
            this.entryFile = new EntryFile(logDir.getEntryFile());
            this.indexFile = new EntryIndexFile(logDir.getIndexFile());
            initialize();
        } catch (IOException e) {
            log.error("failed to open entries file or entry index file.", e);
            throw new LogException("failed to open entries file or entry index file.");
        }
    }

    public FileEntrySequence(int logIndexOffset, EntryFile entryFile, EntryIndexFile indexFile) {
        super(logIndexOffset);
        this.entryFile = entryFile;
        this.indexFile = indexFile;
        initialize();
    }

    private void initialize() {
        if (indexFile.isEmpty()) {
            return;
        }

        // 初始化日志序列索引
        logIndexOffset = indexFile.getMinEntryIndex();
        nextLogIndex = indexFile.getMinEntryIndex() + 1;
    }

    // 获取指定区间的日志
    @Override
    protected List<LogEntry> doSubList(int fromIndex, int toIndex) {
        // 日志范围 [fromIndex, toIndex)
        List<LogEntry> result = new ArrayList<>();

        // 若起始位置在索引文件中，则先从文件中获取可获取的部分
        if (!indexFile.isEmpty() && fromIndex <= indexFile.getMaxEntryIndex()) {
            int maxIndex = Math.min(toIndex, indexFile.getMaxEntryIndex()) + 1;
            for (int i = fromIndex; i < maxIndex; i++) {
                result.add(getEntryInFile(i));
            }
        }

        // 若结束索引大于文件最大索引，则要继续从缓冲区中获取
        if (!pendingEntries.isEmpty() && toIndex > pendingEntries.getFirst().getIndex()) {
            Iterator<LogEntry> iterator = pendingEntries.iterator();
            LogEntry entry;
            int index;
            while (iterator.hasNext()) {
                entry = iterator.next();
                index = entry.getIndex();
                if (index >= toIndex) {
                    break;
                }
                if (index >= fromIndex) {
                    result.add(entry);
                }
            }
        }

        return result;
    }

    private LogEntry getEntryInFile(int index) {
        long offset = indexFile.getOffset(index);
        try {
            return entryFile.loadEntry(offset, entryFactory);
        } catch (IOException e) {
            log.error("failed to load log, index: {}", index);
            throw new LogException(String.format("failed to load log, index: %d", index), e);
        }
    }

    // 获取指定索引的日志
    @Override
    protected LogEntry doGetEntry(int index) {

        // 先尝试从缓冲区中获取
        if (!pendingEntries.isEmpty()) {
            int firstPendingEntryIndex = pendingEntries.getFirst().getIndex();
            if (index >= firstPendingEntryIndex) {
                return pendingEntries.get(index - firstPendingEntryIndex);
            }
        }

        // 若缓冲区未获取到，则从文件获取
        assert !indexFile.isEmpty();
        return getEntryInFile(index);
    }

    // 追加日志，直接放入缓冲区
    @Override
    protected void doAppend(LogEntry entry) {
        pendingEntries.add(entry);
    }


    @Override
    protected void doRemoveAfter(int index) {

        // 如果索引在缓冲区中，只需移除缓冲区中日志
        if (!pendingEntries.isEmpty() && index >= pendingEntries.getFirst().getIndex() - 1) {
            for (int i = index + 1; i <= doGetLastLogIndex(); i++) {
                pendingEntries.removeLast();
            }
            nextLogIndex = index + 1;
            return;
        }

        // 如果索引在文件中，则文件和缓冲区都需要移除日志
        try {
            // 清空部分文件
            if (index >= doGetFirstLogIndex()) {
                pendingEntries.clear();
                entryFile.truncate(indexFile.getOffset(index + 1));
                indexFile.removeAfter(index);
                nextLogIndex = index + 1;
                commitIndex = index;
            }
            // 清空全部文件
            else {
                pendingEntries.clear();
                entryFile.clear();
                indexFile.clear();
                nextLogIndex = logIndexOffset;
                commitIndex = logIndexOffset - 1;
            }
        } catch (IOException e) {
            log.error("remove log failed from index : {}", index);
            throw new LogException("remove log failed from index :" + index, e);
        }
    }

    @Override
    public void commit(int index) {
        // 若提交日志索引小于已提交索引，则抛出异常
        if (index < commitIndex) {
            log.error("index {} is less than commitIndex: {}", index, commitIndex);
            throw new IllegalArgumentException(String.format("index %d is less than commitIndex: %d", index, commitIndex));
        }

        // 若日志已提交，则不重复提交
        if (index == commitIndex) {
            return;
        }

        // 若缓冲区为空，或者提交索引大于缓冲区索引，则抛出异常
        if (pendingEntries.isEmpty()) {
            log.error("no entry to commit， index: {}", index);
            throw new IllegalArgumentException(String.format("no entry to commit， index: %d", index));
        }
        if (pendingEntries.getLast().getIndex() < index) {
            log.error("commit index exceed， index: {}, last index in queue: {}", index, pendingEntries.getLast().getIndex());
            throw new IllegalArgumentException(String.format("commit index exceed， index: %d, last index in queue: %d",
                    index, pendingEntries.getLast().getIndex()));
        }

        long offset;
        LogEntry entry = null;
        try {
            for (int i = commitIndex + 1; i <= index; i++) {
                entry = pendingEntries.removeFirst();
                offset = entryFile.appendEntry(entry);
                indexFile.appendEntryIndex(i, offset, entry.getType(), entry.getTerm());
                commitIndex = i;
            }
        } catch (IOException e) {
            log.error("failed to commit entry " + entry, e);
            throw new LogException("failed to commit entry " + entry, e);
        }
    }

    @Override
    public int getCommitIndex() {
        return commitIndex;
    }

    @Override
    public void close() {
        try {
            entryFile.close();
            indexFile.close();
        } catch (IOException e) {
            log.error("failed to close sequence.", e);
            throw new LogException("failed to close sequence.", e);
        }
    }

    @Override
    public EntryMeta getEntryMeta(int index) {
        if (!isEntryPresent(index)) {
            return null;
        }

        // 若日志在缓冲区中，则直接获取meta
        if (indexFile.isEmpty()) {
            return pendingEntries.get(index - doGetFirstLogIndex()).getMeta();
        }

        // 若日志在文件中，则从索引中获取meta
        return indexFile.get(index).toEntryMeta();
    }

    // 获取最后一条日志
    @Override
    public LogEntry getLastEntry() {
        if (isEmpty()) {
            return null;
        }

        // 尝试从缓冲区获取
        if (!pendingEntries.isEmpty()) {
            return pendingEntries.getLast();
        }

        // 若缓冲区中没有，则从文件中获取
        assert !indexFile.isEmpty();
        return getEntryInFile(indexFile.getMaxEntryIndex());
    }
}
