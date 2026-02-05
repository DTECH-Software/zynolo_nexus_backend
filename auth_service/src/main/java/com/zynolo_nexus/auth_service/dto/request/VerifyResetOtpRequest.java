package com.zynolo_nexus.auth_service.dto.request;

import lombok.Data;

@Data
public class VerifyResetOtpRequest {

    private String username;
    private String otp;
}
