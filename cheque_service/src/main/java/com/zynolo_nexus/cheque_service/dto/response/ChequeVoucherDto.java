package com.zynolo_nexus.cheque_service.dto.response;

import com.zynolo_nexus.cheque_service.enums.ChequeVoucherStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChequeVoucherDto {

    private Long id;
    private String voucherNo;
    private String companyCode;
    private String companyDescription;
    private String customerCode;
    private String customerDescription;
    private String chequeNo;
    private String description;
    private BigDecimal totalAmount;
    private ChequeVoucherStatus status;
    private String statusDescription;
    private List<ChequeVoucherInvoiceDto> invoices;
    private LocalDateTime createdDate;
    private LocalDateTime lastModifiedDate;
    private String createdBy;
    private String lastModifiedBy;
}
