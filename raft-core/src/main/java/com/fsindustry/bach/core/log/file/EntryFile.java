package com.fsindustry.bach.core.log.file;

import com.fsindustry.bach.core.log.entry.EntryFactory;
import com.fsindustry.bach.core.log.entry.LogEntry;
import com.fsindustry.bach.core.support.RandomAccessFileAdapter;
import com.fsindustry.bach.core.support.SeekableFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * 数据文件操作类
 */
public class EntryFile {

    private final SeekableFile seekableFile;

    public EntryFile(File file) throws FileNotFoundException {
        this(new RandomAccessFileAdapter(file));
    }

    public EntryFile(SeekableFile seekableFile) {
        this.seekableFile = seekableFile;
    }

    // 追加日志
    public long appendEntry(LogEntry entry) throws IOException {
        long offset = seekableFile.size();
        seekableFile.seek(offset);
        seekableFile.writeInt(entry.getType());
        seekableFile.writeInt(entry.getIndex());
        seekableFile.writeInt(entry.getTerm());
        byte[] payload = entry.getPayload();
        seekableFile.writeInt(payload.length);
        seekableFile.write(payload);
        // todo 为什么返回旧的offset?
        return offset;
    }

    // 加载指定偏移量的日志
    public LogEntry loadEntry(long offset, EntryFactory factory) throws IOException {
        if (offset > seekableFile.size()) {
            throw new IllegalArgumentException("offset > size");
        }
        seekableFile.seek(offset);
        int kind = seekableFile.readInt();
        int index = seekableFile.readInt();
        int term = seekableFile.readInt();
        int length = seekableFile.readInt();
        byte[] bytes = new byte[length];
        seekableFile.read(bytes);
        return factory.create(kind, index, term, bytes);
    }

    // 获取文件大小
    public long size() throws IOException {
        return seekableFile.size();
    }

    // 截断offset之后的内容；
    public void truncate(long offset) throws IOException {
        seekableFile.truncate(offset);
    }

    // 清理文件内容
    public void clear() throws IOException {
        truncate(0L);
    }

    // 关闭文件
    public void close() throws IOException {
        seekableFile.close();
    }
}
