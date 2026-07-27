package com.zynolo_nexus.meeting_room_booking_service.dto.request;

import jakarta.validation.constraints.Email;
import lombok.Data;

@Data
public class ZynoloSpaceCustomerUpdateRequest {
    private String channel;
    private String ip;
    private String message;
    private String userAgent;
    private String username;
    private Long id;
    private String customerCode;
    private String customerCompanyName;
    private String contactPerson;
    private String contactNumber;

    @Email(message = "Invalid email address")
    private String emailAddress;

    private String address;
    private String remarks;
    private Boolean active;
}
