package com.zynolo_nexus.auth_service.controller.internal;

import com.zynolo_nexus.auth_service.service.RolePageTaskAccessManagementService;
import com.zynolo_nexus.contracts.pages.RolePageTaskAccessDto;
import com.zynolo_nexus.contracts.pages.RolePageTaskAccessUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/role-page-tasks")
@RequiredArgsConstructor
public class RolePageTaskAccessInternalController {

    private final RolePageTaskAccessManagementService rolePageTaskAccessManagementService;

    @PostMapping("/{roleCode}/get")
    public RolePageTaskAccessDto getRolePageTaskAccess(@PathVariable String roleCode) {
        return rolePageTaskAccessManagementService.getRolePageTaskAccess(roleCode);
    }

    @PostMapping("/update")
    public RolePageTaskAccessDto updateRolePageTaskAccess(@RequestBody RolePageTaskAccessUpdateRequest request) {
        return rolePageTaskAccessManagementService.updateRolePageTaskAccess(request);
    }
}
