package com.fsindustry.bach.core.log.entry;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@ToString
@AllArgsConstructor
public abstract class AbstractEntry implements LogEntry {

    @Getter
    int type;
    @Getter
    int index;
    @Getter
    int term;

    @Override
    public EntryMeta getMeta() {
        return new EntryMeta(type, index, term);
    }
}
