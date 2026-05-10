package com.spectrayan.synaptiq.shared.exception;

public class ResourceNotFoundException extends SynaptiqException {
    public ResourceNotFoundException(String message) {
        super(ErrorCode.NOT_FOUND, message);
    }
}
