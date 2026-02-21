package com.zynolo_nexus.cheque_service.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ChequeSupplierStatus {
    ACTIVE("ACTIVE"),
    DEACTIVE("INACTIVE");

    private final String jsonValue;

    ChequeSupplierStatus(String jsonValue) {
        this.jsonValue = jsonValue;
    }

    @JsonCreator
    public static ChequeSupplierStatus fromValue(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim().toUpperCase();
        if ("ACTIVE".equals(normalized)) {
            return ACTIVE;
        }
        if ("INACTIVE".equals(normalized) || "DEACTIVE".equals(normalized)) {
            return DEACTIVE;
        }
        throw new IllegalArgumentException("Unsupported status: " + value);
    }

    @JsonValue
    public String toJsonValue() {
        return jsonValue;
    }
}
