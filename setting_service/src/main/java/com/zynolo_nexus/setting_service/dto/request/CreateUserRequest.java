package com.zynolo_nexus.setting_service.dto.request;

import lombok.Data;
import org.springframework.util.StringUtils;

@Data
public class CreateUserRequest {

    private String channel;
    private String ip;
    private String message;
    private String userAgent;

    private String username;
    private String newUsername;
    private String password;
    private String email;
    private String mobile;
    private String firstName;
    private String lastName;
    private String nic;
    private String company;
    private String roleCode;

    public String getTargetUsername() {
        if (StringUtils.hasText(newUsername)) {
            return newUsername;
        }
        return username;
    }
}
