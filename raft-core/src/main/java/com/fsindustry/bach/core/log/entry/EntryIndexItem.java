package com.fsindustry.bach.core.log.entry;

import lombok.Value;

@Value
public class EntryIndexItem {

    int index;
    long offset;
    int kind;
    int term;

    public EntryMeta toEntryMeta() {
        return new EntryMeta(kind, index, term);
    }
}
