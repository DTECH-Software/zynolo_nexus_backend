package com.zynolo_nexus.setting_service.dto.request;

import com.zynolo_nexus.setting_service.enums.LoginStatus;
import com.zynolo_nexus.setting_service.enums.UserStatus;
import lombok.Data;

@Data
public class UserUpdateByIdRequest {

    private String channel;
    private String ip;
    private String message;
    private String userAgent;
    private String username;

    private Long id;
    private String email;
    private String mobile;
    private String firstName;
    private String lastName;
    private String nic;
    private String company;
    private String roleCode;
    private UserStatus status;
    private LoginStatus loginStatus;
}
