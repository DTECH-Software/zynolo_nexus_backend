package com.zynolo_nexus.meeting_room_booking_service.dto.request;

import lombok.Data;

@Data
public class MeetingVendorFilterSearch {
    private String vendorCode;
    private String vendorName;
    private String vendorType;
    private String contactPerson;
    private String contactNumber;
    private Boolean active;
}
