package com.zynolo_nexus.cheque_service.dto.request;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class ChequeVoucherCreateRequest {

    private String channel;
    private String ip;
    private String message;
    private String userAgent;
    private String username;

    private String companyCode;
    private String customerCode;
    private String chequeNo;
    private String chequeBankName;
    private String chequeType;
    private LocalDate chequeDate;
    private String description;
    private List<ChequeVoucherInvoiceRequest> invoices;
}
