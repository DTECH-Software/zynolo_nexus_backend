package com.zynolo_nexus.setting_service.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.zynolo_nexus.contracts.modules.RoleModuleAccessDto;
import com.zynolo_nexus.contracts.modules.RoleModuleAccessUpdateRequest;
import com.zynolo_nexus.setting_service.dto.api.MessageResponseDTO;
import com.zynolo_nexus.setting_service.service.RoleModuleAccessService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/setting/role-modules")
@RequiredArgsConstructor
public class RoleModuleAccessController {

    private final RoleModuleAccessService roleModuleAccessService;

    @GetMapping("/{roleCode}")
    public MessageResponseDTO<RoleModuleAccessDto> getRoleModuleAccess(@PathVariable String roleCode) {
        return roleModuleAccessService.getRoleModuleAccess(roleCode);
    }

    @PutMapping
    public MessageResponseDTO<RoleModuleAccessDto> updateRoleModuleAccess(
            @RequestBody RoleModuleAccessUpdateRequest request) {
        return roleModuleAccessService.updateRoleModuleAccess(request);
    }
}
