package com.zynolo_nexus.cheque_service.dto.response;

import com.zynolo_nexus.cheque_service.enums.ChequeSupplierStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChequeSupplierDto {

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
    private ChequeSupplierStatus status;
    private String statusDescription;
    private LocalDateTime createdDate;
    private LocalDateTime lastModifiedDate;
    private String createdBy;
    private String lastModifiedBy;
}
