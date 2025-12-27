package com.zynolo_nexus.auth_service.dto.request;

import lombok.Data;

@Data
public class ResetPasswordRequest {

    private String username;
    private String otp;
    private String newPassword;
}
