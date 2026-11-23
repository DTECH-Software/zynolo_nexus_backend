package com.zynolo_nexus.cheque_service.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ChequeReprintStatus {
    REPRINT_PENDING("REPRINT_PENDING"),
    REPRINT_APPROVED("REPRINT_APPROVED"),
    REPRINT_REJECTED("REPRINT_REJECTED");

    private final String jsonValue;

    ChequeReprintStatus(String jsonValue) {
        this.jsonValue = jsonValue;
    }

    @JsonCreator
    public static ChequeReprintStatus fromValue(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim().toUpperCase();
        for (ChequeReprintStatus status : values()) {
            if (status.name().equals(normalized) || status.jsonValue.equals(normalized)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unsupported status: " + value);
    }

    @JsonValue
    public String toJsonValue() {
        return jsonValue;
    }
}
