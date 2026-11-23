package com.zynolo_nexus.auth_service.controller.internal;

import com.zynolo_nexus.auth_service.service.RoleModuleAccessManagementService;
import com.zynolo_nexus.contracts.modules.RoleModuleAccessDto;
import com.zynolo_nexus.contracts.modules.RoleModuleAccessUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/role-modules")
@RequiredArgsConstructor
public class RoleModuleAccessInternalController {

    private final RoleModuleAccessManagementService roleModuleAccessManagementService;

    @PostMapping("/{roleCode}/get")
    public RoleModuleAccessDto getRoleModuleAccess(@PathVariable String roleCode) {
        return roleModuleAccessManagementService.getRoleModuleAccess(roleCode);
    }

    @PostMapping("/update")
    public RoleModuleAccessDto updateRoleModuleAccess(@RequestBody RoleModuleAccessUpdateRequest request) {
        return roleModuleAccessManagementService.updateRoleModuleAccess(request);
    }
}
