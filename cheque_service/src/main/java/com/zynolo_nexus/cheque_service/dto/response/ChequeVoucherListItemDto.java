package com.zynolo_nexus.cheque_service.dto.response;

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
public class ChequeVoucherListItemDto {

    private Long id;
    private String voucherNo;
    private String companyCode;
    private String companyDescription;
    private String customerCode;
    private String customerDescription;
    private String chequeNo;
    private String bankCode;
    private String bankName;
    private String chequeType;
    private LocalDate chequeDate;
    private String description;
    private BigDecimal totalAmount;
    private String status;
    private String statusDescription;
    private String printStatus;
    private String printStatusDescription;
    private Integer printCount;
    private boolean canPrint;
    private boolean requiresReprintApproval;
    private boolean hasPendingReprintRequest;
    private boolean hasApprovedReprintRequest;
    private String nextPrintAction;
    private String lastPrintedBy;
    private LocalDateTime lastPrintedDate;
    private LocalDateTime submittedDate;
    private LocalDateTime approvedDate;
    private LocalDateTime rejectedDate;
    private String rejectionReason;
    private LocalDateTime createdDate;
    private LocalDateTime lastModifiedDate;
    private String createdBy;
    private String lastModifiedBy;
}
