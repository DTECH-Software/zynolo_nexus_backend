package com.zynolo_nexus.setting_service.dto.request;

import com.zynolo_nexus.setting_service.enums.UserCompanyStatus;
import lombok.Data;

@Data
public class UserCompanyCreateRequest {

    private String channel;
    private String ip;
    private String message;
    private String userAgent;
    private String username;
    private Long userId;
    private String userName;
    private Long companyId;
    private String companyCode;
    private Long roleId;
    private String roleCode;
    private UserCompanyStatus status;
    private Boolean isDefault;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}
