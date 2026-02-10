package com.zynolo_nexus.setting_service.controller;

import com.zynolo_nexus.setting_service.dto.api.MessageResponseDTO;
import com.zynolo_nexus.setting_service.dto.request.RoleCreateRequest;
import com.zynolo_nexus.setting_service.dto.request.RoleDeleteRequest;
import com.zynolo_nexus.setting_service.dto.request.RoleFilterRequest;
import com.zynolo_nexus.setting_service.dto.request.RoleReferenceDataRequest;
import com.zynolo_nexus.setting_service.dto.request.RoleStatusUpdateRequest;
import com.zynolo_nexus.setting_service.dto.request.RoleUpdateRequest;
import com.zynolo_nexus.setting_service.dto.request.RoleViewRequest;
import com.zynolo_nexus.setting_service.dto.response.RoleDto;
import com.zynolo_nexus.setting_service.dto.response.RoleFilterResultDto;
import com.zynolo_nexus.setting_service.dto.response.RoleReferenceDataDto;
import com.zynolo_nexus.setting_service.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/setting/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    @PostMapping
    public MessageResponseDTO<RoleDto> createRole(@RequestBody RoleCreateRequest request) {
        return roleService.createRole(request);
    }

    @PostMapping("/update")
    public MessageResponseDTO<RoleDto> updateRole(@RequestBody RoleUpdateRequest request) {
        return roleService.updateRole(request);
    }

    @PostMapping("/view")
    public MessageResponseDTO<RoleDto> viewRole(@RequestBody RoleViewRequest request) {
        Long id = request != null ? request.getId() : null;
        return roleService.viewRole(id);
    }

    @PostMapping("/status")
    public MessageResponseDTO<RoleDto> updateStatus(@RequestBody RoleStatusUpdateRequest request) {
        return roleService.updateRoleStatus(request);
    }

    @PostMapping("/delete")
    public MessageResponseDTO<String> deleteRole(@RequestBody RoleDeleteRequest request) {
        return roleService.deleteRole(request);
    }

    @PostMapping("/filter-list")
    public MessageResponseDTO<RoleFilterResultDto> filterList(@RequestBody RoleFilterRequest request) {
        return roleService.filterList(request);
    }

    @PostMapping("/reference-data")
    public MessageResponseDTO<RoleReferenceDataDto> referenceData(
            @RequestBody(required = false) RoleReferenceDataRequest request) {
        return roleService.getReferenceData(request);
    }
}
