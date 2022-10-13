package com.fsindustry.bach.core.log.entry;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

public enum LogType {

    /**
     * 空日志
     */
    noop(0),

    /**
     * 普通日志
     */
    gernal(1),
    ;

    LogType(int type) {
        this.type = type;
    }

    @Getter
    private final int type;

    private final static Map<Integer, LogType> typeToEnum;
    private final static Map<LogType, Integer> enumToType;

    static {
        LogType[] enums = LogType.values();
        typeToEnum = new HashMap<>(enums.length);
        enumToType = new HashMap<>(enums.length);
        for (LogType e : enums) {
            typeToEnum.put(e.type, e);
            enumToType.put(e, e.type);
        }
    }

    public static LogType getType(int type) {
        return typeToEnum.get(type);
    }
}
