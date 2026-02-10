package com.zynolo_nexus.setting_service.dto.request;

import com.zynolo_nexus.setting_service.enums.UserCompanyStatus;
import lombok.Data;

@Data
public class UserCompanyUpdateRequest {

    private String channel;
    private String ip;
    private String message;
    private String userAgent;
    private String username;
    private Long id;
    private Long companyId;
    private Long roleId;
    private UserCompanyStatus status;
    private Boolean isDefault;
}
