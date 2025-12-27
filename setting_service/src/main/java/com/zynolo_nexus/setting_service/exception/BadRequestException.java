package com.zynolo_nexus.setting_service.exception;

public class BadRequestException extends RuntimeException {

    public BadRequestException(String messageKey) {
        super(messageKey);
    }
}
