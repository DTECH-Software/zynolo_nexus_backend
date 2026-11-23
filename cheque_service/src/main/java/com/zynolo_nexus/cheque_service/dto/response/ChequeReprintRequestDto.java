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
public class ChequeReprintRequestDto {

    private Long id;
    private Long voucherId;
    private String voucherNo;
    private String companyCode;
    private String companyDescription;
    private String customerCode;
    private String customerDescription;
    private String bankCode;
    private String bankName;
    private String chequeNo;
    private LocalDate chequeDate;
    private BigDecimal totalAmount;
    private String voucherStatus;
    private String voucherStatusDescription;
    private String reprintReason;
    private String status;
    private String statusDescription;
    private String requestedBy;
    private LocalDateTime requestedDate;
    private String approvedBy;
    private LocalDateTime approvedDate;
    private String rejectedBy;
    private LocalDateTime rejectedDate;
    private String approvalRemark;
    private String rejectionReason;
    private boolean usedForPrint;
    private LocalDateTime usedDate;
    private LocalDateTime createdDate;
    private LocalDateTime lastModifiedDate;
    private String createdBy;
    private String lastModifiedBy;
}
