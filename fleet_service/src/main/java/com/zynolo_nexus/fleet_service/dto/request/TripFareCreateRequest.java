package com.zynolo_nexus.fleet_service.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class TripFareCreateRequest extends AuditRequest {

    @NotNull(message = "tripId is required")
    private Long tripId;

    @NotNull(message = "amount is required")
    @DecimalMin(value = "0.01", message = "amount must be greater than zero")
    private BigDecimal amount;

    private String currency;

    @NotBlank(message = "paymentMethod is required")
    private String paymentMethod;

    private String notes;
}
