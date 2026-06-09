package com.zynolo_nexus.meeting_room_booking_service.dto.request;

import com.zynolo_nexus.meeting_room_booking_service.enums.SupportAssignedTeam;
import com.zynolo_nexus.meeting_room_booking_service.enums.SupportServiceCategory;
import jakarta.validation.constraints.DecimalMin;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class MeetingSupportServiceUpdateRequest {
    private String channel;
    private String ip;
    private String message;
    private String userAgent;
    private String username;
    private Long id;
    private String serviceCode;
    private String serviceName;
    private SupportServiceCategory serviceCategory;
    private SupportAssignedTeam assignedTeam;
    private Boolean chargeable;

    @DecimalMin(value = "0.00", message = "Default charge cannot be negative")
    private BigDecimal defaultCharge;

    private String description;
    private Boolean active;
}
