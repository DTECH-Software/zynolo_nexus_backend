package com.zynolo_nexus.auth_service.dto.request;

import lombok.Data;

@Data
public class ResetPasswordRequest {

    private String username;
    private String resetToken;
    private String newPassword;
    private String confirmPassword;
}
