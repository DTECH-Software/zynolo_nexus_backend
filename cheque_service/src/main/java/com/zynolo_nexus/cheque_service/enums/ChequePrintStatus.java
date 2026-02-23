package com.zynolo_nexus.cheque_service.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ChequePrintStatus {
    NOT_PRINTED("NOT_PRINTED"),
    PRINTED("PRINTED");

    private final String jsonValue;

    ChequePrintStatus(String jsonValue) {
        this.jsonValue = jsonValue;
    }

    @JsonCreator
    public static ChequePrintStatus fromValue(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim().toUpperCase();
        for (ChequePrintStatus status : values()) {
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
