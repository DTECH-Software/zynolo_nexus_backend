package com.zynolo_nexus.auth_service.exception;

public class ConflictException extends RuntimeException {

    public ConflictException(String messageKey) {
        super(messageKey);
    }
}
