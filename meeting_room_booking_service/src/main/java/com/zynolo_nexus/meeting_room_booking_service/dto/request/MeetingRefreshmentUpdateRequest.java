package com.zynolo_nexus.meeting_room_booking_service.dto.request;

import com.zynolo_nexus.meeting_room_booking_service.enums.RefreshmentCategory;
import jakarta.validation.constraints.DecimalMin;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class MeetingRefreshmentUpdateRequest {
    private String channel;
    private String ip;
    private String message;
    private String userAgent;
    private String username;
    private Long id;
    private String refreshmentCode;
    private RefreshmentCategory category;
    private String itemName;
    private Long defaultVendorId;

    @DecimalMin(value = "0.00", message = "Unit price must be 0 or greater")
    private BigDecimal unitPrice;

    private String description;
    private Boolean active;
}
