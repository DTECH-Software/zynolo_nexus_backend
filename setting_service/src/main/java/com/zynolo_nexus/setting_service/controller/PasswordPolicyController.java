package com.zynolo_nexus.setting_service.controller;

import com.zynolo_nexus.setting_service.dto.api.MessageResponseDTO;
import com.zynolo_nexus.setting_service.dto.request.PasswordPolicyReferenceDataRequest;
import com.zynolo_nexus.setting_service.dto.request.PasswordPolicyResetRequest;
import com.zynolo_nexus.setting_service.dto.request.PasswordPolicyUpdateRequest;
import com.zynolo_nexus.setting_service.dto.request.PasswordPolicyViewRequest;
import com.zynolo_nexus.setting_service.dto.response.PasswordPolicyDto;
import com.zynolo_nexus.setting_service.dto.response.PasswordPolicyReferenceDataDto;
import com.zynolo_nexus.setting_service.service.PasswordPolicyService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/setting/password-policy")
@RequiredArgsConstructor
public class PasswordPolicyController {

    private final PasswordPolicyService passwordPolicyService;

    @PostMapping("/reference-data")
    public MessageResponseDTO<PasswordPolicyReferenceDataDto> referenceData(
            @RequestBody(required = false) PasswordPolicyReferenceDataRequest request) {
        return passwordPolicyService.getReferenceData(request);
    }

    @PostMapping("/view")
    public MessageResponseDTO<PasswordPolicyDto> view(@RequestBody(required = false) PasswordPolicyViewRequest request) {
        return passwordPolicyService.viewPolicy(request);
    }

    @PostMapping("/update")
    public MessageResponseDTO<PasswordPolicyDto> update(@RequestBody PasswordPolicyUpdateRequest request) {
        return passwordPolicyService.updatePolicy(request);
    }

    @PostMapping("/reset")
    public MessageResponseDTO<PasswordPolicyDto> reset(@RequestBody(required = false) PasswordPolicyResetRequest request) {
        return passwordPolicyService.resetPolicy(request);
    }
}
