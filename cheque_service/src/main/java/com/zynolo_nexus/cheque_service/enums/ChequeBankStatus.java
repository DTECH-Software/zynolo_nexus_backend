package com.zynolo_nexus.cheque_service.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ChequeBankStatus {
    ACTIVE("ACTIVE"),
    INACTIVE("INACTIVE");

    private final String jsonValue;

    ChequeBankStatus(String jsonValue) {
        this.jsonValue = jsonValue;
    }

    @JsonCreator
    public static ChequeBankStatus fromValue(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim().toUpperCase();
        if ("ACTIVE".equals(normalized)) {
            return ACTIVE;
        }
        if ("INACTIVE".equals(normalized) || "DEACTIVE".equals(normalized)) {
            return INACTIVE;
        }
        throw new IllegalArgumentException("Unsupported status: " + value);
    }

    @JsonValue
    public String toJsonValue() {
        return jsonValue;
    }
}
