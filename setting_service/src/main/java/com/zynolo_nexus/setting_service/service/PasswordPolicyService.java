package com.zynolo_nexus.setting_service.service;

import com.zynolo_nexus.setting_service.dto.api.MessageResponseDTO;
import com.zynolo_nexus.setting_service.dto.request.PasswordPolicyReferenceDataRequest;
import com.zynolo_nexus.setting_service.dto.request.PasswordPolicyResetRequest;
import com.zynolo_nexus.setting_service.dto.request.PasswordPolicyUpdateRequest;
import com.zynolo_nexus.setting_service.dto.request.PasswordPolicyViewRequest;
import com.zynolo_nexus.setting_service.dto.response.PasswordPolicyDto;
import com.zynolo_nexus.setting_service.dto.response.PasswordPolicyReferenceDataDto;

public interface PasswordPolicyService {

    MessageResponseDTO<PasswordPolicyReferenceDataDto> getReferenceData(PasswordPolicyReferenceDataRequest request);

    MessageResponseDTO<PasswordPolicyDto> viewPolicy(PasswordPolicyViewRequest request);

    MessageResponseDTO<PasswordPolicyDto> updatePolicy(PasswordPolicyUpdateRequest request);

    MessageResponseDTO<PasswordPolicyDto> resetPolicy(PasswordPolicyResetRequest request);
}
