package com.zynolo_nexus.setting_service.service;

import com.zynolo_nexus.setting_service.dto.api.MessageResponseDTO;
import com.zynolo_nexus.setting_service.dto.request.RoleCreateRequest;
import com.zynolo_nexus.setting_service.dto.request.RoleDeleteRequest;
import com.zynolo_nexus.setting_service.dto.request.RoleFilterRequest;
import com.zynolo_nexus.setting_service.dto.request.RoleReferenceDataRequest;
import com.zynolo_nexus.setting_service.dto.request.RoleStatusUpdateRequest;
import com.zynolo_nexus.setting_service.dto.request.RoleUpdateRequest;
import com.zynolo_nexus.setting_service.dto.response.RoleDto;
import com.zynolo_nexus.setting_service.dto.response.RoleFilterResultDto;
import com.zynolo_nexus.setting_service.dto.response.RoleReferenceDataDto;

public interface RoleService {

    MessageResponseDTO<RoleDto> createRole(RoleCreateRequest request);

    MessageResponseDTO<RoleDto> updateRole(RoleUpdateRequest request);

    MessageResponseDTO<RoleDto> viewRole(Long id);

    MessageResponseDTO<RoleDto> updateRoleStatus(RoleStatusUpdateRequest request);

    MessageResponseDTO<String> deleteRole(RoleDeleteRequest request);

    MessageResponseDTO<RoleFilterResultDto> filterList(RoleFilterRequest request);

    MessageResponseDTO<RoleReferenceDataDto> getReferenceData(RoleReferenceDataRequest request);
}
