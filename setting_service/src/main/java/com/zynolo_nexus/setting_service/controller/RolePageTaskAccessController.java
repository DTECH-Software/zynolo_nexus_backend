package com.zynolo_nexus.setting_service.controller;

import com.zynolo_nexus.contracts.pages.RolePageTaskAccessDto;
import com.zynolo_nexus.contracts.pages.RolePageTaskAccessUpdateRequest;
import com.zynolo_nexus.setting_service.dto.api.MessageResponseDTO;
import com.zynolo_nexus.setting_service.dto.request.RolePageTaskAccessCheckRequest;
import com.zynolo_nexus.setting_service.dto.request.RolePageTaskAccessPreviewRequest;
import com.zynolo_nexus.setting_service.dto.request.RolePageTaskAccessReferenceDataRequest;
import com.zynolo_nexus.setting_service.dto.request.RolePageTaskAccessUpdateByIdRequest;
import com.zynolo_nexus.setting_service.dto.request.RolePageTaskAccessViewRequest;
import com.zynolo_nexus.setting_service.dto.response.RolePageTaskAccessReferenceDataDto;
import com.zynolo_nexus.setting_service.dto.response.RolePageTaskPrivilegeCheckDto;
import com.zynolo_nexus.setting_service.dto.response.RolePageTaskPrivilegePreviewDto;
import com.zynolo_nexus.setting_service.service.RolePageTaskAccessService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/setting/role-page-tasks")
@RequiredArgsConstructor
public class RolePageTaskAccessController {

    private final RolePageTaskAccessService rolePageTaskAccessService;

    @PostMapping("/{roleCode}/get")
    public MessageResponseDTO<RolePageTaskAccessDto> getRolePageTaskAccess(@PathVariable String roleCode) {
        return rolePageTaskAccessService.getRolePageTaskAccess(roleCode);
    }

    @PostMapping("/view")
    public MessageResponseDTO<RolePageTaskAccessDto> getRolePageTaskAccess(@RequestBody RolePageTaskAccessViewRequest request) {
        return rolePageTaskAccessService.getRolePageTaskAccess(request);
    }

    @PostMapping("/update")
    public MessageResponseDTO<RolePageTaskAccessDto> updateRolePageTaskAccess(
            @RequestBody RolePageTaskAccessUpdateRequest request) {
        return rolePageTaskAccessService.updateRolePageTaskAccess(request);
    }

    @PostMapping("/update-by-id")
    public MessageResponseDTO<RolePageTaskAccessDto> updateRolePageTaskAccessById(
            @RequestBody RolePageTaskAccessUpdateByIdRequest request) {
        return rolePageTaskAccessService.updateRolePageTaskAccess(request);
    }

    @PostMapping("/check")
    public MessageResponseDTO<RolePageTaskPrivilegeCheckDto> checkRolePageTaskAccess(
            @RequestBody RolePageTaskAccessCheckRequest request) {
        return rolePageTaskAccessService.checkRolePageTaskAccess(request);
    }

    @PostMapping("/reference-data")
    public MessageResponseDTO<RolePageTaskAccessReferenceDataDto> referenceData(
            @RequestBody(required = false) RolePageTaskAccessReferenceDataRequest request) {
        return rolePageTaskAccessService.getReferenceData(request);
    }

    @PostMapping("/preview")
    public MessageResponseDTO<RolePageTaskPrivilegePreviewDto> previewPageTasks(
            @RequestBody RolePageTaskAccessPreviewRequest request) {
        return rolePageTaskAccessService.previewPageTasks(request);
    }
}
