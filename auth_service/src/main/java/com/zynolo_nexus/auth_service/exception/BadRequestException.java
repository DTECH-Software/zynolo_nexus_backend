package com.zynolo_nexus.auth_service.exception;

public class BadRequestException extends RuntimeException {

    public BadRequestException(String messageKey) {
        super(messageKey);
    }
}
