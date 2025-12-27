package com.zynolo_nexus.setting_service.service;

import com.zynolo_nexus.contracts.pages.RolePageTaskAccessDto;
import com.zynolo_nexus.contracts.pages.RolePageTaskAccessUpdateRequest;
import com.zynolo_nexus.setting_service.dto.api.MessageResponseDTO;

public interface RolePageTaskAccessService {

    MessageResponseDTO<RolePageTaskAccessDto> getRolePageTaskAccess(String roleCode);

    MessageResponseDTO<RolePageTaskAccessDto> updateRolePageTaskAccess(RolePageTaskAccessUpdateRequest request);
}
