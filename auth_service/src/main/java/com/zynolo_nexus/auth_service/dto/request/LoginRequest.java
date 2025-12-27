package com.zynolo_nexus.auth_service.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {

    private String ip;
    private String message;
    private String userAgent;
    private String username;
    private String password;
}
