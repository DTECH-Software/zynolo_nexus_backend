package com.zynolo_nexus.meeting_room_booking_service.dto.request;

import com.zynolo_nexus.meeting_room_booking_service.enums.MeetingVendorType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MeetingVendorCreateRequest {
    private String channel;
    private String ip;
    private String message;
    private String userAgent;
    private String username;

    @NotBlank(message = "Vendor code is required")
    private String vendorCode;

    @NotBlank(message = "Vendor name is required")
    private String vendorName;

    @NotNull(message = "Vendor type is required")
    private MeetingVendorType vendorType;

    private String contactPerson;
    private String contactNumber;

    @Email(message = "Invalid email address")
    private String emailAddress;

    private String address;
    private String remarks;

    @NotNull(message = "Active status is required")
    private Boolean active;
}
