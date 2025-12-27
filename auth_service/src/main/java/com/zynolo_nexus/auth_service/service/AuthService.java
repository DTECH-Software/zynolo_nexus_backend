package com.zynolo_nexus.auth_service.service;

import com.zynolo_nexus.auth_service.dto.api.MessageResponseDTO;
import com.zynolo_nexus.auth_service.dto.request.ForgotPasswordRequest;
import com.zynolo_nexus.auth_service.dto.request.LoginRequest;
import com.zynolo_nexus.auth_service.dto.request.LogoutRequest;
import com.zynolo_nexus.auth_service.dto.request.ResetPasswordRequest;
import com.zynolo_nexus.auth_service.dto.response.LoginData;
import com.zynolo_nexus.auth_service.dto.response.ReferenceDataDto;

public interface AuthService {

    MessageResponseDTO<LoginData> login(LoginRequest request);

    MessageResponseDTO<String> logout(LogoutRequest request);

    MessageResponseDTO<String> forgotPassword(ForgotPasswordRequest request);

    MessageResponseDTO<String> resetPassword(ResetPasswordRequest request);

    MessageResponseDTO<ReferenceDataDto> getReferenceData(String username);
}
