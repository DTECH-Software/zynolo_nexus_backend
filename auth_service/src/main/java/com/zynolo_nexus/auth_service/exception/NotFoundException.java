package com.zynolo_nexus.auth_service.exception;

public class NotFoundException extends RuntimeException {

    public NotFoundException(String messageKey) {
        super(messageKey);
    }
}
