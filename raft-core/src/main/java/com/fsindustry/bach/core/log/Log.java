package com.fsindustry.bach.core.log;

import com.fsindustry.bach.core.connector.msg.vo.AppendEntriesRpc;
import com.fsindustry.bach.core.log.entry.EntryMeta;
import com.fsindustry.bach.core.log.entry.GeneralEntry;
import com.fsindustry.bach.core.log.entry.LogEntry;
import com.fsindustry.bach.core.log.entry.NoopEntry;
import com.fsindustry.bach.core.node.Node;

import java.util.List;

/**
 * 日志操作接口
 */
public interface Log {

    // 获取最新日志的元信息
    EntryMeta getLastEntryMeta();

    // 创建AppendEntriesRPC
    AppendEntriesRpc createAppendEntriesRpc(int term, Node selfId, int nextIndex, int maxEntries);

    // 获取下一条日志索引
    int getNextIndex();

    // 获取当前的CommitIndex
    int getCommitIndex();

    // 判断传入的lastLogIndex和lastLogTerm是否比自己新
    boolean isNewerThan(int lastLogIndex, int lastLogTerm);

    // 新建一个no-op日志；
    NoopEntry appendEntry(int term);

    // 新建一条普通日志；
    GeneralEntry appendEntry(int term, byte[] command);

    // 追加来自Leader的一组日志；
    boolean appendEntriesFromLeader(int prevLogIndex, int prevLogTerm, List<LogEntry> entries);

    // 推进commitIndex
    void advanceCommitIndex(int newCommitIndex, int currentTerm);

    // 关闭
    void close();
}
