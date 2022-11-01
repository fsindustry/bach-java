package com.fsindustry.batch.kvstore.cmd;

import lombok.Value;

/**
 * 发生异常时的响应
 */
@Value
public class FailureResp {
    int errorCode;
    String message;
}
