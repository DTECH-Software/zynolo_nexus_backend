package com.zynolo_nexus.setting_service.dto.request;

import lombok.Data;

@Data
public class CreateUserRequest {

    private String username;
    private String password;
    private String email;
    private String mobile;
    private String firstName;
    private String lastName;
    private String nic;
    private String company;
    private String roleCode;
}
