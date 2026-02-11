package com.zynolo_nexus.setting_service.controller;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.zynolo_nexus.contracts.modules.RoleModuleAccessDto;
import com.zynolo_nexus.contracts.modules.RoleModuleAccessUpdateRequest;
import com.zynolo_nexus.setting_service.dto.api.MessageResponseDTO;
import com.zynolo_nexus.setting_service.dto.request.RoleModuleAccessCheckRequest;
import com.zynolo_nexus.setting_service.dto.request.RoleModuleAccessReferenceDataRequest;
import com.zynolo_nexus.setting_service.dto.request.RoleModuleAccessUpdateByIdRequest;
import com.zynolo_nexus.setting_service.dto.request.RoleModuleAccessViewRequest;
import com.zynolo_nexus.setting_service.dto.response.RoleModuleAccessReferenceDataDto;
import com.zynolo_nexus.setting_service.dto.response.RoleModulePrivilegeCheckDto;
import com.zynolo_nexus.setting_service.service.RoleModuleAccessService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/setting/role-modules")
@RequiredArgsConstructor
public class RoleModuleAccessController {

    private final RoleModuleAccessService roleModuleAccessService;

    @PostMapping("/{roleCode}/get")
    public MessageResponseDTO<RoleModuleAccessDto> getRoleModuleAccess(@PathVariable String roleCode) {
        return roleModuleAccessService.getRoleModuleAccess(roleCode);
    }

    @PostMapping("/view")
    public MessageResponseDTO<RoleModuleAccessDto> getRoleModuleAccess(@RequestBody RoleModuleAccessViewRequest request) {
        return roleModuleAccessService.getRoleModuleAccess(request);
    }

    @PostMapping("/update")
    public MessageResponseDTO<RoleModuleAccessDto> updateRoleModuleAccess(
            @RequestBody RoleModuleAccessUpdateRequest request) {
        return roleModuleAccessService.updateRoleModuleAccess(request);
    }

    @PostMapping("/update-by-id")
    public MessageResponseDTO<RoleModuleAccessDto> updateRoleModuleAccessById(
            @RequestBody RoleModuleAccessUpdateByIdRequest request) {
        return roleModuleAccessService.updateRoleModuleAccess(request);
    }

    @PostMapping("/check")
    public MessageResponseDTO<RoleModulePrivilegeCheckDto> checkRoleModuleAccess(
            @RequestBody RoleModuleAccessCheckRequest request) {
        return roleModuleAccessService.checkRoleModuleAccess(request);
    }

    @PostMapping("/reference-data")
    public MessageResponseDTO<RoleModuleAccessReferenceDataDto> referenceData(
            @RequestBody(required = false) RoleModuleAccessReferenceDataRequest request) {
        return roleModuleAccessService.getReferenceData(request);
    }
}
