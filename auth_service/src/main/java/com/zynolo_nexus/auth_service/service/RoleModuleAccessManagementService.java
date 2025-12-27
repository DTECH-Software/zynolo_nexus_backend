package com.zynolo_nexus.auth_service.service;

import com.zynolo_nexus.contracts.modules.RoleModuleAccessDto;
import com.zynolo_nexus.contracts.modules.RoleModuleAccessUpdateRequest;

public interface RoleModuleAccessManagementService {

    RoleModuleAccessDto getRoleModuleAccess(String roleCode);

    RoleModuleAccessDto updateRoleModuleAccess(RoleModuleAccessUpdateRequest request);
}
