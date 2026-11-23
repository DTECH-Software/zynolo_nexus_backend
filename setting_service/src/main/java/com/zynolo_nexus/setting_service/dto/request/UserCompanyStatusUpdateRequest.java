package com.zynolo_nexus.setting_service.dto.request;

import com.zynolo_nexus.setting_service.enums.UserCompanyStatus;
import lombok.Data;

@Data
public class UserCompanyStatusUpdateRequest {

    private String channel;
    private String ip;
    private String message;
    private String userAgent;
    private String username;
    private Long id;
    private UserCompanyStatus status;
}
