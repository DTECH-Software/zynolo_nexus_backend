package com.zynolo_nexus.meeting_room_booking_service.dto.request;

import com.zynolo_nexus.meeting_room_booking_service.enums.MeetingVendorType;
import jakarta.validation.constraints.Email;
import lombok.Data;

@Data
public class MeetingVendorUpdateRequest {
    private String channel;
    private String ip;
    private String message;
    private String userAgent;
    private String username;
    private Long id;
    private String vendorCode;
    private String vendorName;
    private MeetingVendorType vendorType;
    private String contactPerson;
    private String contactNumber;

    @Email(message = "Invalid email address")
    private String emailAddress;

    private String address;
    private String remarks;
    private Boolean active;
}
