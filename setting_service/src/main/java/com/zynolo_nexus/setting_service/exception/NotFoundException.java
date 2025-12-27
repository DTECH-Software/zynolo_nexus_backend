package com.zynolo_nexus.setting_service.exception;

public class NotFoundException extends RuntimeException {

    public NotFoundException(String messageKey) {
        super(messageKey);
    }
}
