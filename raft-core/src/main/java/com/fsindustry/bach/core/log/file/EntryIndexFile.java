package com.fsindustry.bach.core.log.file;

import com.fsindustry.bach.core.log.entry.EntryIndexItem;
import com.fsindustry.bach.core.support.RandomAccessFileAdapter;
import com.fsindustry.bach.core.support.SeekableFile;
import lombok.Getter;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * 索引文件定义
 * 索引文件为定长文件
 */
public class EntryIndexFile implements Iterable<EntryIndexItem> {

    // max entry index的起始偏移量
    private static final long OFFSET_MAX_ENTRY_INDEX = Integer.BYTES;
    // 单条entry index的长度
    private static final int LENGTH_ENTRY_INDEX_ITEM = 16;

    private final SeekableFile seekableFile;

    // 日志条目数
    private int entryIndexCount;
    // 最小日志索引
    @Getter
    private int minEntryIndex;
    // 最大日志索引
    @Getter
    private int maxEntryIndex;

    // ( idx, item )
    private Map<Integer, EntryIndexItem> entryIndexMap = new HashMap<>();

    public EntryIndexFile(File file) throws IOException {
        this(new RandomAccessFileAdapter(file));
    }

    public EntryIndexFile(SeekableFile seekableFile) throws IOException {
        this.seekableFile = seekableFile;
        load();
    }

    // 加载所有日志元信息
    private void load() throws IOException {
        if (seekableFile.size() == 0L) {
            entryIndexCount = 0;
            return;
        }

        minEntryIndex = seekableFile.readInt();
        maxEntryIndex = seekableFile.readInt();
        // 更新日志索引数量
        updateEntryIndexCount();
        long offset;
        int kind;
        int term;
        for (int i = minEntryIndex; i <= maxEntryIndex; i++) {
            offset = seekableFile.readLong();
            kind = seekableFile.readInt();
            term = seekableFile.readInt();
            entryIndexMap.put(i, new EntryIndexItem(i, offset, kind, term));
        }
    }

    private void updateEntryIndexCount() {
        entryIndexCount = maxEntryIndex - minEntryIndex + 1;
    }

    // 追加日志索引
    public void appendEntryIndex(int index, long offset, int kind, int term) throws IOException {

        // 更新minEntryIndex
        if (seekableFile.size() == 0) {
            seekableFile.writeInt(index);
            minEntryIndex = index;
        } else {
            if (index != maxEntryIndex + 1) {
                throw new IllegalArgumentException(String.format("invalid entry index. expect: %d, actual: %d", maxEntryIndex + 1, index));
            }
            seekableFile.seek(OFFSET_MAX_ENTRY_INDEX);
        }

        // 更新maxEntryIndex
        seekableFile.writeInt(index);
        maxEntryIndex = index;
        updateEntryIndexCount();

        // 写入索引数据
        seekableFile.seek(getOffsetOfEntryIndexItem(index));
        seekableFile.writeLong(offset);
        seekableFile.writeInt(kind);
        seekableFile.writeInt(term);
        entryIndexMap.put(index, new EntryIndexItem(index, offset, kind, term));
    }

    // 计算指定索引所在文件的偏移量
    public long getOffsetOfEntryIndexItem(int index) {
        return (long) (index - minEntryIndex) * LENGTH_ENTRY_INDEX_ITEM + Integer.BYTES * 2;
    }

    private void checkEmpty() {
        if (isEmpty()) {
            throw new IllegalStateException("no entry index");
        }
    }

    @Nonnull
    public EntryIndexItem get(int entryIndex) {
        checkEmpty();
        if (entryIndex < minEntryIndex || entryIndex > maxEntryIndex) {
            throw new IllegalArgumentException("index < min or index > max");
        }
        return entryIndexMap.get(entryIndex);
    }

    public long getOffset(int entryIndex) {
        return get(entryIndex).getOffset();
    }

    // 清空日志
    public void clear() throws IOException {
        seekableFile.truncate(0L);
        entryIndexCount = 0;
        entryIndexMap.clear();
    }


    public void removeAfter(int newMaxEntryIndex) throws IOException {

        // 边界条件1
        if (isEmpty() || newMaxEntryIndex >= maxEntryIndex) {
            return;
        }

        // 边界条件2
        if (newMaxEntryIndex < minEntryIndex) {
            clear();
            return;
        }

        // 更新maxEntryIndex
        seekableFile.seek(OFFSET_MAX_ENTRY_INDEX);
        seekableFile.writeInt(newMaxEntryIndex);
        // 裁剪文件
        seekableFile.truncate(getOffsetOfEntryIndexItem(newMaxEntryIndex + 1));
        // 更新内存索引
        for (int i = newMaxEntryIndex + 1; i <= maxEntryIndex; i++) {
            entryIndexMap.remove(i);
        }
        maxEntryIndex = newMaxEntryIndex;
        updateEntryIndexCount();
    }

    public boolean isEmpty() {
        return entryIndexCount == 0;
    }

    @Override
    public Iterator<EntryIndexItem> iterator() {
        if (isEmpty()) {
            return Collections.emptyIterator();
        }
        return new EntryIndexIterator(entryIndexCount, minEntryIndex);
    }

    public void close() throws IOException {
        seekableFile.close();
    }

    /**
     * 索引文件迭代器
     */
    private class EntryIndexIterator implements Iterator<EntryIndexItem> {

        // 日志条目数
        private final int entryIndexCount;
        // 当前索引
        private int currentEntryIndex;

        public EntryIndexIterator(int entryIndexCount, int minEntryIndex) {
            this.entryIndexCount = entryIndexCount;
            this.currentEntryIndex = minEntryIndex;
        }

        @Override
        public boolean hasNext() {
            checkModification();
            return currentEntryIndex <= maxEntryIndex;
        }


        @Override
        public EntryIndexItem next() {
            checkModification();
            return entryIndexMap.get(currentEntryIndex++);
        }

        private void checkModification() {
            if (entryIndexCount != EntryIndexFile.this.entryIndexCount) {
                throw new IllegalStateException(String.format("file index count has changed, original: %d, current: %d", entryIndexCount, EntryIndexFile.this.entryIndexCount));
            }
        }
    }
}
