package com.fsindustry.bach.core.log.entry;

import lombok.ToString;

/**
 * 空日志
 */
@ToString
public class NoopEntry extends AbstractEntry {

    public NoopEntry(int index, int term) {
        super(LogType.noop.getType(), index, term);
    }

    @Override
    public byte[] getPayload() {
        return new byte[0];
    }
}
