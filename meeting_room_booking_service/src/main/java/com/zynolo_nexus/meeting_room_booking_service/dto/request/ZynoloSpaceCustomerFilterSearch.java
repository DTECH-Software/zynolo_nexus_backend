package com.zynolo_nexus.meeting_room_booking_service.dto.request;

import lombok.Data;

@Data
public class ZynoloSpaceCustomerFilterSearch {
    private String customerCode;
    private String customerCompanyName;
    private String contactPerson;
    private String contactNumber;
    private String emailAddress;
    private Boolean active;
    private String status;
}
