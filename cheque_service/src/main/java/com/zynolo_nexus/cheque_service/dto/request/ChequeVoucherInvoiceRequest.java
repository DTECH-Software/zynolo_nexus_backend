package com.zynolo_nexus.cheque_service.dto.request;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ChequeVoucherInvoiceRequest {

    private LocalDate invoiceDate;
    private String invoiceNo;
    private String description;
    private BigDecimal amount;
}
