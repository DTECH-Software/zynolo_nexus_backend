package com.zynolo_nexus.cheque_service.dto.request;

import com.zynolo_nexus.cheque_service.enums.ChequeCustomerStatus;
import lombok.Data;

@Data
public class ChequeCustomerUpdateRequest {

    private String channel;
    private String ip;
    private String message;
    private String userAgent;
    private String username;

    private Long id;
    private String code;
    private String description;
    private String street1;
    private String street2;
    private String city;
    private String state;
    private String country;
    private String zipCode;
    private String contactNo;
    private String email;
    private String website;
    private ChequeCustomerStatus status;
}
