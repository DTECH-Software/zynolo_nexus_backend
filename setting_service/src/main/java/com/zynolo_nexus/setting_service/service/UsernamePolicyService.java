package com.zynolo_nexus.setting_service.service;

import com.zynolo_nexus.setting_service.dto.api.MessageResponseDTO;
import com.zynolo_nexus.setting_service.dto.request.UsernamePolicyReferenceDataRequest;
import com.zynolo_nexus.setting_service.dto.request.UsernamePolicyResetRequest;
import com.zynolo_nexus.setting_service.dto.request.UsernamePolicyUpdateRequest;
import com.zynolo_nexus.setting_service.dto.request.UsernamePolicyViewRequest;
import com.zynolo_nexus.setting_service.dto.response.UsernamePolicyDto;
import com.zynolo_nexus.setting_service.dto.response.UsernamePolicyReferenceDataDto;

public interface UsernamePolicyService {

    MessageResponseDTO<UsernamePolicyReferenceDataDto> getReferenceData(UsernamePolicyReferenceDataRequest request);

    MessageResponseDTO<UsernamePolicyDto> viewPolicy(UsernamePolicyViewRequest request);

    MessageResponseDTO<UsernamePolicyDto> updatePolicy(UsernamePolicyUpdateRequest request);

    MessageResponseDTO<UsernamePolicyDto> resetPolicy(UsernamePolicyResetRequest request);
}
