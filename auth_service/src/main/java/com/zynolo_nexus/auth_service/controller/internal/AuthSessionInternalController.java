package com.zynolo_nexus.auth_service.controller.internal;

import com.zynolo_nexus.auth_service.dto.request.InternalSessionValidationRequest;
import com.zynolo_nexus.auth_service.dto.response.InternalSessionValidationResponse;
import com.zynolo_nexus.auth_service.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/internal/auth-sessions")
@RequiredArgsConstructor
public class AuthSessionInternalController {

    private final RefreshTokenRepository refreshTokenRepository;

    @PostMapping("/validate")
    @Transactional(readOnly = true)
    public InternalSessionValidationResponse validate(@RequestBody InternalSessionValidationRequest request) {
        boolean active = request != null
                && StringUtils.hasText(request.getUsername())
                && StringUtils.hasText(request.getSessionId())
                && refreshTokenRepository.existsByUsernameAndSessionIdAndExpiresAtAfter(
                request.getUsername().trim(),
                request.getSessionId().trim(),
                LocalDateTime.now()
        );
        return InternalSessionValidationResponse.builder()
                .active(active)
                .build();
    }
}
