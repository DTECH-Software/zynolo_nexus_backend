package com.zynolo_nexus.setting_service.dto.request;

import com.zynolo_nexus.setting_service.enums.CompanyStatus;
import lombok.Data;

@Data
public class CompanyUpdateRequest {

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
    private String phoneNumber;
    private String mobileNumber;
    private String email;
    private String website;
    private String taxId;
    private String logo;
    private CompanyStatus status;
}
