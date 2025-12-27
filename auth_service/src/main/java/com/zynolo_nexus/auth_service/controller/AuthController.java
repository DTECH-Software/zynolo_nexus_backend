package com.zynolo_nexus.auth_service.controller;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.zynolo_nexus.auth_service.dto.api.MessageResponseDTO;
import com.zynolo_nexus.auth_service.dto.request.ForgotPasswordRequest;
import com.zynolo_nexus.auth_service.dto.request.LoginRequest;
import com.zynolo_nexus.auth_service.dto.request.LogoutRequest;
import com.zynolo_nexus.auth_service.dto.request.ResetPasswordRequest;
import com.zynolo_nexus.auth_service.dto.response.LoginData;
import com.zynolo_nexus.auth_service.dto.response.ReferenceDataDto;
import com.zynolo_nexus.auth_service.service.AuthService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public MessageResponseDTO<LoginData> login(@RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/logout")
    public MessageResponseDTO<String> logout(@RequestBody LogoutRequest request) {
        return authService.logout(request);
    }

    @PostMapping("/forgot-password")
    public MessageResponseDTO<String> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        return authService.forgotPassword(request);
    }

    @PostMapping("/reset-password")
    public MessageResponseDTO<String> resetPassword(@RequestBody ResetPasswordRequest request) {
        return authService.resetPassword(request);
    }

    @GetMapping("/reference-data")
    public MessageResponseDTO<ReferenceDataDto> referenceData(Authentication authentication) {
        String username = authentication.getName();
        return authService.getReferenceData(username);
    }
}
