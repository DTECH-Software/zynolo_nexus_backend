package com.zynolo_nexus.meeting_room_booking_service.dto.request;

import com.zynolo_nexus.meeting_room_booking_service.enums.SupportAssignedTeam;
import com.zynolo_nexus.meeting_room_booking_service.enums.SupportServiceCategory;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class MeetingSupportServiceCreateRequest {
    private String channel;
    private String ip;
    private String message;
    private String userAgent;
    private String username;

    @NotBlank(message = "Service code is required")
    private String serviceCode;

    @NotBlank(message = "Service name is required")
    private String serviceName;

    @NotNull(message = "Service category is required")
    private SupportServiceCategory serviceCategory;

    private SupportAssignedTeam assignedTeam;

    @NotNull(message = "Chargeable status is required")
    private Boolean chargeable;

    @DecimalMin(value = "0.00", message = "Default charge cannot be negative")
    private BigDecimal defaultCharge;

    private String description;

    @NotNull(message = "Active status is required")
    private Boolean active;
}
