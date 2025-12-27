package com.zynolo_nexus.auth_service.dto.request;

import lombok.Data;

@Data
public class ForgotPasswordRequest {

    private String username;
    private String email;
}
