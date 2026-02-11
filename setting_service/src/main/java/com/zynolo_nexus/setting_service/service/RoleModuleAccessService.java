package com.zynolo_nexus.setting_service.service;

import com.zynolo_nexus.contracts.modules.RoleModuleAccessDto;
import com.zynolo_nexus.contracts.modules.RoleModuleAccessUpdateRequest;
import com.zynolo_nexus.setting_service.dto.api.MessageResponseDTO;
import com.zynolo_nexus.setting_service.dto.request.RoleModuleAccessCheckRequest;
import com.zynolo_nexus.setting_service.dto.request.RoleModuleAccessReferenceDataRequest;
import com.zynolo_nexus.setting_service.dto.request.RoleModuleAccessUpdateByIdRequest;
import com.zynolo_nexus.setting_service.dto.request.RoleModuleAccessViewRequest;
import com.zynolo_nexus.setting_service.dto.response.RoleModuleAccessReferenceDataDto;
import com.zynolo_nexus.setting_service.dto.response.RoleModulePrivilegeCheckDto;

public interface RoleModuleAccessService {

    MessageResponseDTO<RoleModuleAccessDto> getRoleModuleAccess(String roleCode);

    MessageResponseDTO<RoleModuleAccessDto> updateRoleModuleAccess(RoleModuleAccessUpdateRequest request);

    MessageResponseDTO<RoleModuleAccessDto> getRoleModuleAccess(RoleModuleAccessViewRequest request);

    MessageResponseDTO<RoleModuleAccessDto> updateRoleModuleAccess(RoleModuleAccessUpdateByIdRequest request);

    MessageResponseDTO<RoleModulePrivilegeCheckDto> checkRoleModuleAccess(RoleModuleAccessCheckRequest request);

    MessageResponseDTO<RoleModuleAccessReferenceDataDto> getReferenceData(RoleModuleAccessReferenceDataRequest request);
}
