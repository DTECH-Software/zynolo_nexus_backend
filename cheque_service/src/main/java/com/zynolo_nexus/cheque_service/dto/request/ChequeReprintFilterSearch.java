package com.zynolo_nexus.cheque_service.dto.request;

import lombok.Data;

@Data
public class ChequeReprintFilterSearch {

    private String voucherNo;
    private String companyCode;
    private String customerCode;
    private String bankCode;
    private String status;
}
