package com.zynolo_nexus.auth_service.service;

import com.zynolo_nexus.contracts.pages.RolePageTaskAccessDto;
import com.zynolo_nexus.contracts.pages.RolePageTaskAccessUpdateRequest;

public interface RolePageTaskAccessManagementService {

    RolePageTaskAccessDto getRolePageTaskAccess(String roleCode);

    RolePageTaskAccessDto updateRolePageTaskAccess(RolePageTaskAccessUpdateRequest request);
}
