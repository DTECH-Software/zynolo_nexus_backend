package com.zynolo_nexus.setting_service.dto.request;

import com.zynolo_nexus.setting_service.enums.CompanyStatus;
import lombok.Data;

@Data
public class CompanyCreateRequest {

    private String channel;
    private String ip;
    private String message;
    private String userAgent;
    private String username;
    private String code;
    private String description;
    private CompanyStatus status;
}
