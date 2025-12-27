package com.zynolo_nexus.auth_service.dto.request.validator;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.zynolo_nexus.auth_service.dto.request.LoginRequest;

@Component
public class LoginRequestValidator {

    public void validate(LoginRequest request) {

        if (request == null) {
            throw new IllegalArgumentException("Login request cannot be null");
        }

        if (!StringUtils.hasText(request.getUsername())) {
            throw new IllegalArgumentException("Username is required");
        }

        if (!StringUtils.hasText(request.getPassword())) {
            throw new IllegalArgumentException("Password is required");
        }
    }
}
