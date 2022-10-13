package com.fsindustry.bach.core.support;

import java.io.IOException;
import java.io.InputStream;

/**
 * 抽象文件操作接口，方便测试
 */
public interface SeekableFile {

    long position() throws IOException;

    void seek(long position) throws IOException;

    void writeInt(int i) throws IOException;

    void writeLong(long l) throws IOException;

    void write(byte[] b) throws IOException;

    int readInt() throws IOException;

    long readLong() throws IOException;

    int read(byte[] b) throws IOException;

    long size() throws IOException;

    void truncate(long size) throws IOException;

    InputStream inputStream(long start) throws IOException;

    void flush() throws IOException;

    void close() throws IOException;
}
