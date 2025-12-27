package com.zynolo_nexus.auth_service.exception;

public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException(String messageKey) {
        super(messageKey);
    }
}
