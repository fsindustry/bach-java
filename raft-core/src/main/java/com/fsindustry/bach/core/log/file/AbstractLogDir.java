package com.fsindustry.bach.core.log.file;

import com.fsindustry.bach.core.log.exception.LogException;
import com.google.common.io.Files;

import java.io.File;
import java.io.IOException;

public abstract class AbstractLogDir implements LogDir {

    final File dir;

    AbstractLogDir(File dir) {
        this.dir = dir;
    }

    @Override
    public void initialize() {
        if (!dir.exists() && !dir.mkdir()) {
            throw new LogException("failed to create directory " + dir);
        }
        try {
            Files.touch(getEntryFile());
            Files.touch(getIndexFile());
        } catch (IOException e) {
            throw new LogException("failed to create file", e);
        }
    }

    @Override
    public boolean exists() {
        return dir.exists();
    }

    @Override
    public File getSnapshotFile() {
        return new File(dir, RootDir.FILE_NAME_SNAPSHOT);
    }

    @Override
    public File getEntryFile() {
        return new File(dir, RootDir.FILE_NAME_ENTRIES);
    }

    @Override
    public File getIndexFile() {
        return new File(dir, RootDir.FILE_NAME_ENTRY_OFFSET_INDEX);
    }

    @Override
    public File get() {
        return dir;
    }

    @Override
    public boolean renameTo(LogDir logDir) {
        return dir.renameTo(logDir.get());
    }

}
