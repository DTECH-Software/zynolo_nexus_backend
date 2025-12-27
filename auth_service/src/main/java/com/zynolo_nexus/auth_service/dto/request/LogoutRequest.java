package com.zynolo_nexus.auth_service.dto.request;

import lombok.Data;

@Data
public class LogoutRequest {
    private String refreshToken;
}
