package com.zynolo_nexus.setting_service.controller;

import com.zynolo_nexus.contracts.pages.RolePageTaskAccessDto;
import com.zynolo_nexus.contracts.pages.RolePageTaskAccessUpdateRequest;
import com.zynolo_nexus.setting_service.dto.api.MessageResponseDTO;
import com.zynolo_nexus.setting_service.service.RolePageTaskAccessService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/setting/role-page-tasks")
@RequiredArgsConstructor
public class RolePageTaskAccessController {

    private final RolePageTaskAccessService rolePageTaskAccessService;

    @GetMapping("/{roleCode}")
    public MessageResponseDTO<RolePageTaskAccessDto> getRolePageTaskAccess(@PathVariable String roleCode) {
        return rolePageTaskAccessService.getRolePageTaskAccess(roleCode);
    }

    @PutMapping
    public MessageResponseDTO<RolePageTaskAccessDto> updateRolePageTaskAccess(
            @RequestBody RolePageTaskAccessUpdateRequest request) {
        return rolePageTaskAccessService.updateRolePageTaskAccess(request);
    }
}
