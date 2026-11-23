package com.zynolo_nexus.meeting_room_booking_service.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class MeetingBeverageCreateRequest {
    private String channel;
    private String ip;
    private String message;
    private String userAgent;
    private String username;

    @NotBlank(message = "Beverage code is required")
    private String beverageCode;

    @NotBlank(message = "Beverage name is required")
    private String beverageName;

    private Long defaultVendorId;

    @NotNull(message = "Unit price is required")
    @DecimalMin(value = "0.00", message = "Unit price must be 0 or greater")
    private BigDecimal unitPrice;

    private String description;

    @NotNull(message = "Active status is required")
    private Boolean active;
}
