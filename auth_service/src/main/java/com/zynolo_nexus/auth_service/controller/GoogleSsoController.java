package com.zynolo_nexus.auth_service.controller;

import com.zynolo_nexus.auth_service.dto.api.MessageResponseDTO;
import com.zynolo_nexus.auth_service.dto.request.GoogleSsoRequest;
import com.zynolo_nexus.auth_service.dto.response.LoginData;
import com.zynolo_nexus.auth_service.service.GoogleSsoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth/sso")
@RequiredArgsConstructor
public class GoogleSsoController {

    private final GoogleSsoService googleSsoService;

    @PostMapping("/google")
    public MessageResponseDTO<LoginData> loginWithGoogle(@RequestBody GoogleSsoRequest request) {
        return googleSsoService.loginWithGoogle(request != null ? request.getIdToken() : null);
    }
}
