package com.oncoresi.domain.exception;

/**
 * 领域异常
 * 用于封装业务规则违反的情况
 */
public class DomainException extends RuntimeException {

    public DomainException(String message) {
        super(message);
    }

    public DomainException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * 创建领域异常
     */
    public static DomainException of(String message) {
        return new DomainException(message);
    }

    /**
     * 创建带原因的领域异常
     */
    public static DomainException of(String message, Throwable cause) {
        return new DomainException(message, cause);
    }
}
