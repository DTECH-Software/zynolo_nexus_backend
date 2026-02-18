package com.zynolo_nexus.cheque_service.dto.request;

import lombok.Data;

@Data
public class ChequeCompanyFilterSearch {

    private String code;
    private String description;
    private String status;
}

