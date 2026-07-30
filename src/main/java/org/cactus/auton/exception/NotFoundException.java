package org.cactus.auton.exception;

/**
 * 资源未找到异常（Command 无对应 Node、Actor 不存在等）。
 */
public class NotFoundException extends BehaviorException {
    public NotFoundException() {
        super();
    }

    public NotFoundException(String message) {
        super(message);
    }

    public NotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
