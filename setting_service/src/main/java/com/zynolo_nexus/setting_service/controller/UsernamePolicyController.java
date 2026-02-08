package com.zynolo_nexus.setting_service.controller;

import com.zynolo_nexus.setting_service.dto.api.MessageResponseDTO;
import com.zynolo_nexus.setting_service.dto.request.UsernamePolicyReferenceDataRequest;
import com.zynolo_nexus.setting_service.dto.request.UsernamePolicyResetRequest;
import com.zynolo_nexus.setting_service.dto.request.UsernamePolicyUpdateRequest;
import com.zynolo_nexus.setting_service.dto.request.UsernamePolicyViewRequest;
import com.zynolo_nexus.setting_service.dto.response.UsernamePolicyDto;
import com.zynolo_nexus.setting_service.dto.response.UsernamePolicyReferenceDataDto;
import com.zynolo_nexus.setting_service.service.UsernamePolicyService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/setting/username-policy")
@RequiredArgsConstructor
public class UsernamePolicyController {

    private final UsernamePolicyService usernamePolicyService;

    @PostMapping("/reference-data")
    public MessageResponseDTO<UsernamePolicyReferenceDataDto> referenceData(
            @RequestBody(required = false) UsernamePolicyReferenceDataRequest request) {
        return usernamePolicyService.getReferenceData(request);
    }

    @PostMapping("/view")
    public MessageResponseDTO<UsernamePolicyDto> view(@RequestBody(required = false) UsernamePolicyViewRequest request) {
        return usernamePolicyService.viewPolicy(request);
    }

    @PostMapping("/update")
    public MessageResponseDTO<UsernamePolicyDto> update(@RequestBody UsernamePolicyUpdateRequest request) {
        return usernamePolicyService.updatePolicy(request);
    }

    @PostMapping("/reset")
    public MessageResponseDTO<UsernamePolicyDto> reset(@RequestBody(required = false) UsernamePolicyResetRequest request) {
        return usernamePolicyService.resetPolicy(request);
    }
}
