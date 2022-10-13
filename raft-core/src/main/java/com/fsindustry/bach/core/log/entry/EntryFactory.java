package com.fsindustry.bach.core.log.entry;

public class EntryFactory {

    public LogEntry create(int kind, int index, int term, byte[] payload) {
        switch (LogType.getType(kind)) {
            case gernal:
                return new GeneralEntry(index, term, payload);
            case noop:
                return new NoopEntry(index, term);
            default:
                throw new IllegalArgumentException("unknown log type:" + kind);
        }
    }
}
