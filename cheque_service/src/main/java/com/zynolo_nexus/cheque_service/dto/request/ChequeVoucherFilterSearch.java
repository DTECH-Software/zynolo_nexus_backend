package com.zynolo_nexus.cheque_service.dto.request;

import lombok.Data;

@Data
public class ChequeVoucherFilterSearch {

    private String voucherNo;
    private String companyCode;
    private String customerCode;
    private String chequeNo;
    private String chequeBankName;
    private String chequeType;
    private String status;
}
