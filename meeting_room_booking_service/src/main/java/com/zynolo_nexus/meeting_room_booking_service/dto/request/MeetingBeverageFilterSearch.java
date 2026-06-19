package com.zynolo_nexus.meeting_room_booking_service.dto.request;

import lombok.Data;

@Data
public class MeetingBeverageFilterSearch {
    private String beverageCode;
    private String beverageName;
    private Long defaultVendorId;
    private String defaultVendorName;
    private Boolean active;
    private String status;
}

