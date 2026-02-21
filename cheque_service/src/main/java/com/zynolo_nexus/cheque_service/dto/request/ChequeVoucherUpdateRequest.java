package com.zynolo_nexus.cheque_service.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class ChequeVoucherUpdateRequest {

    private String channel;
    private String ip;
    private String message;
    private String userAgent;
    private String username;

    private Long id;
    private String companyCode;
    private String customerCode;
    private String chequeNo;
    private String description;
    private List<ChequeVoucherInvoiceRequest> invoices;
}
