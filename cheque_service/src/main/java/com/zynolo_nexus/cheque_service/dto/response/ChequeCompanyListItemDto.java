package com.zynolo_nexus.cheque_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChequeCompanyListItemDto {

    private Long id;
    private String code;
    private String description;
    private String street1;
    private String street2;
    private String city;
    private String state;
    private String country;
    private String zipCode;
    private String phoneNumber;
    private String mobileNumber;
    private String email;
    private String website;
    private String taxId;
    private String status;
    private String statusDescription;
    private LocalDateTime createdDate;
    private LocalDateTime lastModifiedDate;
    private String createdBy;
    private String lastModifiedBy;
}
