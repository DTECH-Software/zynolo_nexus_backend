package com.zynolo_nexus.auth_service.controller;

import org.springframework.security.core.Authentication;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import com.zynolo_nexus.auth_service.dto.api.MessageResponseDTO;
import com.zynolo_nexus.auth_service.dto.request.ForgotPasswordRequest;
import com.zynolo_nexus.auth_service.dto.request.ChangePasswordRequest;
import com.zynolo_nexus.auth_service.dto.request.LoginRequest;
import com.zynolo_nexus.auth_service.dto.request.LogoutRequest;
import com.zynolo_nexus.auth_service.dto.request.MainDashboardRequest;
import com.zynolo_nexus.auth_service.dto.request.ResetPasswordRequest;
import com.zynolo_nexus.auth_service.dto.request.VerifyResetOtpRequest;
import com.zynolo_nexus.auth_service.dto.response.LoginData;
import com.zynolo_nexus.auth_service.dto.response.ResetTokenResponse;
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

    @PostMapping("/verify-reset-otp")
    public MessageResponseDTO<ResetTokenResponse> verifyResetOtp(@RequestBody VerifyResetOtpRequest request) {
        return authService.verifyResetOtp(request);
    }

    @PostMapping("/reset-password")
    public MessageResponseDTO<String> resetPassword(@RequestBody ResetPasswordRequest request) {
        return authService.resetPassword(request);
    }

    @PostMapping("/change-password")
    public MessageResponseDTO<String> changePassword(
            Authentication authentication,
            @RequestBody ChangePasswordRequest request) {
        String username = authentication.getName();
        return authService.changePassword(username, request);
    }

    @PostMapping("/main-dashboard")
    public MessageResponseDTO<ReferenceDataDto> mainDashboard(
            Authentication authentication,
            @RequestBody(required = false) MainDashboardRequest request) {
        String username = authentication.getName();
        if (request != null && StringUtils.hasText(request.getUsername())
                && !request.getUsername().equals(username)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Invalid username");
        }
        return authService.getReferenceData(username);
    }
}
