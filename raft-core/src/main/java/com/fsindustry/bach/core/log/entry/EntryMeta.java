package com.fsindustry.bach.core.log.entry;

import lombok.Value;

@Value
public class EntryMeta {
    int type;
    int index;
    int term;
}
