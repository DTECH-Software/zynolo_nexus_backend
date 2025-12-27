package com.zynolo_nexus.auth_service.controller.internal;

import com.zynolo_nexus.auth_service.service.RolePageTaskAccessManagementService;
import com.zynolo_nexus.contracts.pages.RolePageTaskAccessDto;
import com.zynolo_nexus.contracts.pages.RolePageTaskAccessUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/role-page-tasks")
@RequiredArgsConstructor
public class RolePageTaskAccessInternalController {

    private final RolePageTaskAccessManagementService rolePageTaskAccessManagementService;

    @GetMapping("/{roleCode}")
    public RolePageTaskAccessDto getRolePageTaskAccess(@PathVariable String roleCode) {
        return rolePageTaskAccessManagementService.getRolePageTaskAccess(roleCode);
    }

    @PutMapping
    public RolePageTaskAccessDto updateRolePageTaskAccess(@RequestBody RolePageTaskAccessUpdateRequest request) {
        return rolePageTaskAccessManagementService.updateRolePageTaskAccess(request);
    }
}
