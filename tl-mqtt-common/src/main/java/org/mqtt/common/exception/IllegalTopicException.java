package org.mqtt.common.exception;

public class IllegalTopicException extends RuntimeException {

    private static final long serialVersionUID = -5365630128856068164L;

    public IllegalTopicException() {
    }

    public IllegalTopicException(String msg) {
        super(msg);
    }

    public IllegalTopicException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public IllegalTopicException(Throwable cause) {
        super(cause);
    }
}
