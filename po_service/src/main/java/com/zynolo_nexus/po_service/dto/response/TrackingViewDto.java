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
public class TrackingViewDto {
    private Long id;
    private Long requestId;
    private String poNo;
    private String requestNo;
    private String companyCode;
    private String companyName;
    private String requestType;
    private String requestStatus;
    private String requestStatusDescription;
    private String department;
    private String costCenter;
    private String currencyCode;
    private String vendorCode;
    private String vendorName;
    private LocalDate requiredDate;
    private String justification;
    private String poStatus;
    private String poStatusDescription;
    private String matchStatus;
    private String matchStatusDescription;
    private String paymentStatus;
    private String paymentStatusDescription;
    private BigDecimal totalAmount;
    private BigDecimal totalInvoicedAmount;
    private BigDecimal totalPaidAmount;
    private BigDecimal balanceAmount;
    private LocalDateTime requestSubmittedDate;
    private LocalDateTime requestReviewedDate;
    private String requestReviewedBy;
    private String requestReviewRemark;
    private LocalDateTime sentDate;
    private String sentBy;
    private String sendRemark;
    private LocalDateTime vendorConfirmationDate;
    private String vendorConfirmationBy;
    private String vendorReferenceNo;
    private LocalDate expectedDeliveryDate;
    private String vendorConfirmationRemark;
    private LocalDateTime matchedDate;
    private String matchedBy;
    private String matchRemark;
    private LocalDate lastInvoiceDate;
    private String lastInvoiceNo;
    private String lastInvoiceRemark;
    private LocalDate lastPaymentDate;
    private String lastChequeNo;
    private String lastPaymentMethod;
    private String lastPaymentReferenceNo;
    private String lastPaymentRemark;
    private LocalDateTime createdDate;
    private LocalDateTime lastModifiedDate;
    private String createdBy;
    private String lastModifiedBy;
    private List<TrackingItemDto> items;
}
