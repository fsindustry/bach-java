package com.fsindustry.bach.core.log.file;

import lombok.extern.slf4j.Slf4j;

import java.io.File;

@Slf4j
public class RootDir {

    static final String FILE_NAME_SNAPSHOT = "service.ss";
    static final String FILE_NAME_ENTRIES = "entries.bin";
    static final String FILE_NAME_ENTRY_OFFSET_INDEX = "entries.idx";
    private static final String DIR_NAME_GENERATING = "generating";
    private static final String DIR_NAME_INSTALLING = "installing";

    private final File baseDir;

    public RootDir(File baseDir) {
        if (!baseDir.exists()) {
            throw new IllegalArgumentException("dir " + baseDir + " not exists");
        }
        this.baseDir = baseDir;
    }

    public LogDir getLogDirForGenerating() {
        return getOrCreateNormalLogDir(DIR_NAME_GENERATING);
    }

    public LogDir getLogDirForInstalling() {
        return getOrCreateNormalLogDir(DIR_NAME_INSTALLING);
    }

    private NormalLogDir getOrCreateNormalLogDir(String name) {
        NormalLogDir logDir = new NormalLogDir(new File(baseDir, name));
        if (!logDir.exists()) {
            logDir.initialize();
        }
        return logDir;
    }

    public LogDir rename(LogDir dir, int lastIncludedIndex) {
        LogGeneration destDir = new LogGeneration(baseDir, lastIncludedIndex);
        if (destDir.exists()) {
            throw new IllegalStateException("failed to rename, dest dir " + destDir + " exists");
        }

        log.info("rename dir {} to {}", dir, destDir);
        if (!dir.renameTo(destDir)) {
            throw new IllegalStateException("failed to rename " + dir + " to " + destDir);
        }
        return destDir;
    }

    public LogGeneration createFirstGeneration() {
        LogGeneration generation = new LogGeneration(baseDir, 0);
        generation.initialize();
        return generation;
    }

    public LogGeneration getLatestGeneration() {
        File[] files = baseDir.listFiles();
        if (files == null) {
            return null;
        }
        LogGeneration latest = null;
        String fileName;
        LogGeneration generation;
        for (File file : files) {
            if (!file.isDirectory()) {
                continue;
            }
            fileName = file.getName();
            if (DIR_NAME_GENERATING.equals(fileName) || DIR_NAME_INSTALLING.equals(fileName) ||
                    !LogGeneration.isValidDirName(fileName)) {
                continue;
            }
            generation = new LogGeneration(file);
            if (latest == null || generation.compareTo(latest) > 0) {
                latest = generation;
            }
        }
        return latest;
    }

}