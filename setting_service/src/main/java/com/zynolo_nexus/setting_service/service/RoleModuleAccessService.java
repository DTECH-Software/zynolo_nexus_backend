package com.zynolo_nexus.setting_service.service;

import com.zynolo_nexus.contracts.modules.RoleModuleAccessDto;
import com.zynolo_nexus.contracts.modules.RoleModuleAccessUpdateRequest;
import com.zynolo_nexus.setting_service.dto.api.MessageResponseDTO;

public interface RoleModuleAccessService {

    MessageResponseDTO<RoleModuleAccessDto> getRoleModuleAccess(String roleCode);

    MessageResponseDTO<RoleModuleAccessDto> updateRoleModuleAccess(RoleModuleAccessUpdateRequest request);
}
