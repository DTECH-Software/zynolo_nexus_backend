package com.zynolo_nexus.setting_service.dto.request;

import lombok.Data;

@Data
public class UserFilterSearch {

    private String username;
    private String role;
    private String nic;
    private String email;
    private String mobile;
    private String firstName;
    private String lastName;
    private String status;
    private String loginStatus;
}
