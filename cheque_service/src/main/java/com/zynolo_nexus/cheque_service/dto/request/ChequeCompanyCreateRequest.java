package com.zynolo_nexus.cheque_service.dto.request;

import com.zynolo_nexus.cheque_service.enums.ChequeCompanyStatus;
import com.zynolo_nexus.cheque_service.dto.common.LogoDocumentDto;
import lombok.Data;

@Data
public class ChequeCompanyCreateRequest {

    private String channel;
    private String ip;
    private String message;
    private String userAgent;
    private String username;

    private String code;
    private String description;
    private String street1;
    private String street2;
    private String city;
    private String state;
    private String country;
    private String zipCode;
    private String phoneNumber;
    private String mobileNumber;
    private String email;
    private String website;
    private String taxId;
    private LogoDocumentDto logo;
    private ChequeCompanyStatus status;
}
