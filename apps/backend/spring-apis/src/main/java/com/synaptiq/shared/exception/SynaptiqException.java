package com.synaptiq.shared.exception;

import lombok.Getter;

/**
 * Base application exception with error code support.
 */
@Getter
public class SynaptiqException extends RuntimeException {

    private final ErrorCode errorCode;

    public SynaptiqException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public SynaptiqException(ErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
}
