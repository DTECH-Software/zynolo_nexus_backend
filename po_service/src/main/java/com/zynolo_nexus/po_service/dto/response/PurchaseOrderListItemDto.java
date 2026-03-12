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
public class PurchaseOrderListItemDto {
    private Long id;
    private String poNo;
    private String requestNo;
    private String companyCode;
    private String companyName;
    private String vendorCode;
    private String vendorName;
    private String requestType;
    private String status;
    private String statusDescription;
    private BigDecimal totalAmount;
    private LocalDate requiredDate;
    private LocalDate expectedDeliveryDate;
    private LocalDateTime sentDate;
    private LocalDateTime createdDate;
    private LocalDateTime lastModifiedDate;
    private String createdBy;
    private String lastModifiedBy;
}
