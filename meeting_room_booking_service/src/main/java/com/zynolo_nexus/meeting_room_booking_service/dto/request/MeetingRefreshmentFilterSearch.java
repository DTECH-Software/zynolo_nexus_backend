package com.zynolo_nexus.meeting_room_booking_service.dto.request;

import lombok.Data;

@Data
public class MeetingRefreshmentFilterSearch {
    private String refreshmentCode;
    private String category;
    private String itemName;
    private Long defaultVendorId;
    private String defaultVendorName;
    private Boolean active;
    private String status;
}

