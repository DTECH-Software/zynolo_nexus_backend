package com.zynolo_nexus.po_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VendorDto {
    private Long id;
    private String code;
    private String description;
    private String street1;
    private String street2;
    private String city;
    private String state;
    private String country;
    private String zipCode;
    private String contactNo;
    private String email;
    private String website;
    private String status;
    private String statusDescription;
    private LocalDateTime createdDate;
    private LocalDateTime lastModifiedDate;
    private String createdBy;
    private String lastModifiedBy;
}
