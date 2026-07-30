package org.cactus.auton.exception;

/**
 * 行为树框架基础异常。
 */
public class BehaviorException extends RuntimeException {
    public BehaviorException() {
    }

    public BehaviorException(String message) {
        super(message);
    }

    public BehaviorException(String message, Throwable cause) {
        super(message, cause);
    }

    public BehaviorException(Throwable cause) {
        super(cause);
    }

    public BehaviorException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
