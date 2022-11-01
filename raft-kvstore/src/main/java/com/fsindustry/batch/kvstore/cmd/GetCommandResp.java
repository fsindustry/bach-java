package com.fsindustry.batch.kvstore.cmd;

import lombok.Value;

@Value
public class GetCommandResp {
    boolean found;
    byte[] value;
}
