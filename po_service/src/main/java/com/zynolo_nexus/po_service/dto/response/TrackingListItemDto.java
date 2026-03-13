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
public class TrackingListItemDto {
    private Long id;
    private String poNo;
    private String requestNo;
    private String companyCode;
    private String companyName;
    private String vendorCode;
    private String vendorName;
    private String requestType;
    private String poStatus;
    private String poStatusDescription;
    private String matchStatus;
    private String matchStatusDescription;
    private String paymentStatus;
    private String paymentStatusDescription;
    private BigDecimal totalAmount;
    private LocalDate requiredDate;
    private LocalDateTime createdDate;
    private LocalDateTime lastModifiedDate;
    private String createdBy;
    private String lastModifiedBy;
}
