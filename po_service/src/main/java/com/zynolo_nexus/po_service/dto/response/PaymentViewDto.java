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
public class PaymentViewDto {
    private Long id;
    private String poNo;
    private String requestNo;
    private String companyCode;
    private String companyName;
    private String vendorCode;
    private String vendorName;
    private String currencyCode;
    private String department;
    private String costCenter;
    private String status;
    private String statusDescription;
    private String matchStatus;
    private String matchStatusDescription;
    private BigDecimal totalAmount;
    private BigDecimal totalInvoicedAmount;
    private BigDecimal totalPaidAmount;
    private BigDecimal balanceAmount;
    private LocalDate lastPaymentDate;
    private String lastChequeNo;
    private String lastPaymentMethod;
    private String lastPaymentReferenceNo;
    private String lastPaymentRemark;
    private LocalDateTime createdDate;
    private LocalDateTime lastModifiedDate;
    private String createdBy;
    private String lastModifiedBy;
    private List<PaymentItemBalanceDto> items;
}
