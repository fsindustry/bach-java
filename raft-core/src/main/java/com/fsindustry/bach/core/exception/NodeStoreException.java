package com.fsindustry.bach.core.exception;

public class NodeStoreException extends RuntimeException {
    public NodeStoreException() {
    }

    public NodeStoreException(String message) {
        super(message);
    }

    public NodeStoreException(String message, Throwable cause) {
        super(message, cause);
    }

    public NodeStoreException(Throwable cause) {
        super(cause);
    }

    public NodeStoreException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
