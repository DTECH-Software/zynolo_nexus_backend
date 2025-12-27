package com.zynolo_nexus.auth_service.controller.internal;

import com.zynolo_nexus.auth_service.service.RoleModuleAccessManagementService;
import com.zynolo_nexus.contracts.modules.RoleModuleAccessDto;
import com.zynolo_nexus.contracts.modules.RoleModuleAccessUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/role-modules")
@RequiredArgsConstructor
public class RoleModuleAccessInternalController {

    private final RoleModuleAccessManagementService roleModuleAccessManagementService;

    @GetMapping("/{roleCode}")
    public RoleModuleAccessDto getRoleModuleAccess(@PathVariable String roleCode) {
        return roleModuleAccessManagementService.getRoleModuleAccess(roleCode);
    }

    @PutMapping
    public RoleModuleAccessDto updateRoleModuleAccess(@RequestBody RoleModuleAccessUpdateRequest request) {
        return roleModuleAccessManagementService.updateRoleModuleAccess(request);
    }
}
