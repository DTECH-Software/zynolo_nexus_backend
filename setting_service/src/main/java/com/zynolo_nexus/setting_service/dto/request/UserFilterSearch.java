package com.zynolo_nexus.setting_service.dto.request;

import lombok.Data;
import org.springframework.util.StringUtils;

@Data
public class UserFilterSearch {

    private String username;
    private String newUsername;
    private String role;
    private String nic;
    private String email;
    private String mobile;
    private String firstName;
    private String lastName;
    private String status;
    private String loginStatus;

    public String getSearchUsername() {
        if (StringUtils.hasText(newUsername)) {
            return newUsername;
        }
        return username;
    }
}
