package com.zynolo_nexus.po_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PoRequestDto {
    private Long id;
    private String requestNo;
    private String companyCode;
    private String companyName;
    private String requestType;
    private String department;
    private String departmentDescription;
    private String costCenter;
    private String currencyCode;
    private String vendorCode;
    private String vendorName;
    private LocalDate requiredDate;
    private String justification;
    private String status;
    private String statusDescription;
    private BigDecimal totalAmount;
    private LocalDateTime submittedDate;
    private LocalDateTime reviewedDate;
    private String reviewedBy;
    private String reviewRemark;
    private String rejectionReason;
    private LocalDateTime createdDate;
    private LocalDateTime lastModifiedDate;
    private String createdBy;
    private String lastModifiedBy;
    private List<PoRequestItemDto> items;
}
