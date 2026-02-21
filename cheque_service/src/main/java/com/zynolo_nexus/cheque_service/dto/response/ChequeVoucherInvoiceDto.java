package com.zynolo_nexus.cheque_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChequeVoucherInvoiceDto {

    private Integer lineNo;
    private LocalDate invoiceDate;
    private String invoiceNo;
    private String description;
    private BigDecimal amount;
}
