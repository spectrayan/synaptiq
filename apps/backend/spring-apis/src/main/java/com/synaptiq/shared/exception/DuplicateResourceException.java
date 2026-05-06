package com.synaptiq.shared.exception;

public class DuplicateResourceException extends SynaptiqException {
    public DuplicateResourceException(String message) {
        super(ErrorCode.DUPLICATE_RESOURCE, message);
    }
}
