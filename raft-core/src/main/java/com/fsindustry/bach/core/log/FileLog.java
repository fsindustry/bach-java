package com.fsindustry.bach.core.log;

import com.fsindustry.bach.core.log.file.LogGeneration;
import com.fsindustry.bach.core.log.file.RootDir;
import com.fsindustry.bach.core.log.sequence.FileEntrySequence;

import java.io.File;

public class FileLog extends AbstractLog {

    private final RootDir rootDir;

    public FileLog(File baseDir) {
        // 创建相关目录
        this.rootDir = new RootDir(baseDir);

        // 查找最新的日志文件目录，若存在则基于该目录同步日志
        LogGeneration latest = rootDir.getLatestGeneration();
        if (latest != null) {
            // 初始化日志序列，传入日志起始索引
            sequence = new FileEntrySequence(latest, latest.getLastIncludedIndex() + 1);
        }
        // 若目录不存在，则创建第一个目录
        else {
            LogGeneration first = rootDir.createFirstGeneration();
            sequence = new FileEntrySequence(first, 1);
        }

    }
}
