package com.zynolo_nexus.setting_service.dto.request;

import lombok.Data;

@Data
public class CompanyModuleUpdateRequest {

    private String channel;
    private String ip;
    private String message;
    private String userAgent;
    private String username;
    private Long id;
    private String companyCode;
    private String moduleCode;
    private Boolean enabled;
}
