package com.zynolo_nexus.cheque_service.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ChequeVoucherStatus {
    DRAFT("DRAFT"),
    PENDING_APPROVAL("PENDING_APPROVAL"),
    APPROVED("APPROVED"),
    REJECTED("REJECTED"),
    CHEQUE_CREATED("CHEQUE_CREATED");

    private final String jsonValue;

    ChequeVoucherStatus(String jsonValue) {
        this.jsonValue = jsonValue;
    }

    @JsonCreator
    public static ChequeVoucherStatus fromValue(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim().toUpperCase();
        for (ChequeVoucherStatus status : values()) {
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
