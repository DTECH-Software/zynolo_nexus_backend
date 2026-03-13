package com.zynolo_nexus.po_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ThreeWayMatchListItemDto {
    private Long id;
    private String poNo;
    private String requestNo;
    private String companyCode;
    private String companyName;
    private String vendorCode;
    private String vendorName;
    private String status;
    private String statusDescription;
    private String matchStatus;
    private String matchStatusDescription;
    private BigDecimal totalAmount;
    private String lastInvoiceNo;
    private LocalDate lastInvoiceDate;
    private LocalDateTime createdDate;
    private LocalDateTime lastModifiedDate;
    private String createdBy;
    private String lastModifiedBy;
}
