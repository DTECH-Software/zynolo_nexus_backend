package com.zynolo_nexus.meeting_room_booking_service.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ZynoloSpaceCustomerCreateRequest {
    private String channel;
    private String ip;
    private String message;
    private String userAgent;
    private String username;

    @NotBlank(message = "Customer code is required")
    private String customerCode;

    @NotBlank(message = "Customer company name is required")
    private String customerCompanyName;

    private String contactPerson;
    private String contactNumber;

    @Email(message = "Invalid email address")
    private String emailAddress;

    private String address;
    private String remarks;

    @NotNull(message = "Active status is required")
    private Boolean active;
}
