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
public class ThreeWayMatchViewDto {
    private Long id;
    private String poNo;
    private String requestNo;
    private String companyCode;
    private String companyName;
    private String vendorCode;
    private String vendorName;
    private String currencyCode;
    private String status;
    private String statusDescription;
    private String matchStatus;
    private String matchStatusDescription;
    private BigDecimal totalAmount;
    private String lastInvoiceNo;
    private LocalDate lastInvoiceDate;
    private LocalDateTime matchedDate;
    private String matchedBy;
    private String matchRemark;
    private LocalDateTime createdDate;
    private LocalDateTime lastModifiedDate;
    private String createdBy;
    private String lastModifiedBy;
    private List<ThreeWayMatchLineDto> items;
}
