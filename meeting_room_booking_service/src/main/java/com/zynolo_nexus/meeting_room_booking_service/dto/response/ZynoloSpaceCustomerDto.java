package com.zynolo_nexus.meeting_room_booking_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ZynoloSpaceCustomerDto {
    private Long id;
    private Long companyId;
    private String companyCode;
    private String companyName;
    private String customerCode;
    private String customerCompanyName;
    private String contactPerson;
    private String contactNumber;
    private String emailAddress;
    private String address;
    private String remarks;
    private Boolean active;
    private String activeStatusDescription;
    private Boolean selectable;
    private LocalDateTime createdDate;
    private LocalDateTime lastModifiedDate;
    private String createdBy;
    private String lastModifiedBy;
}
