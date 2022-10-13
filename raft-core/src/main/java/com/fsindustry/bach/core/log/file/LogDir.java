package com.fsindustry.bach.core.log.file;

import java.io.File;

/**
 * 定义日志存放目录
 */
public interface LogDir {

    // 初始化目录
    void initialize();

    // 判断目录是否存在
    boolean exists();

    // 获取快照文件
    File getSnapshotFile();

    // 获取日志文件
    File getEntriesFile();

    // 获取索引文件
    File getEntryOffsetIndexFile();

    // 获取目录
    File get();

    // 重命名目录
    boolean renameTo(LogDir logDir);
}