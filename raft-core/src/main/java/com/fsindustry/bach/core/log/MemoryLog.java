package com.fsindustry.bach.core.log;

import com.fsindustry.bach.core.log.sequence.EntrySequence;
import com.fsindustry.bach.core.log.sequence.MemoryEntrySequence;

public class MemoryLog extends AbstractLog {

    public MemoryLog() {
        this(new MemoryEntrySequence());
    }

    public MemoryLog(EntrySequence entrySequence) {
        this.sequence = entrySequence;
    }


}
