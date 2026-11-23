package com.zynolo_nexus.cheque_service.dto.request;

import lombok.Data;

@Data
public class ChequeCustomerFilterSearch {

    private String code;
    private String description;
    private String contactNo;
    private String email;
    private String status;
}
