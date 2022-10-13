package com.fsindustry.bach.core.log.entry;

import lombok.ToString;

/**
 * 普通日志
 */
@ToString
public class GeneralEntry extends AbstractEntry {

    private final byte[] payload;

    public GeneralEntry(int index, int term, byte[] payload) {
        super(LogType.gernal.getType(), index, term);
        this.payload = payload;
    }

    @Override
    public byte[] getPayload() {
        return payload;
    }
}
