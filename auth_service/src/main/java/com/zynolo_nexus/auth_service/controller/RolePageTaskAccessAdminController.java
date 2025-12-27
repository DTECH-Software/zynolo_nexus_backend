package com.zynolo_nexus.auth_service.controller;

import com.zynolo_nexus.auth_service.dto.api.MessageResponseDTO;
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

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/role-page-tasks")
@RequiredArgsConstructor
public class RolePageTaskAccessAdminController {

    private final RolePageTaskAccessManagementService rolePageTaskAccessManagementService;

    @GetMapping("/{roleCode}")
    public MessageResponseDTO<RolePageTaskAccessDto> getAccess(@PathVariable String roleCode) {
        RolePageTaskAccessDto dto = rolePageTaskAccessManagementService.getRolePageTaskAccess(roleCode);
        return MessageResponseDTO.<RolePageTaskAccessDto>builder()
                .success(true)
                .message("role.page.task.fetch.success")
                .data(dto)
                .errors(null)
                .errorCode(0)
                .responseTime(LocalDateTime.now())
                .build();
    }

    @PutMapping
    public MessageResponseDTO<RolePageTaskAccessDto> updateAccess(@RequestBody RolePageTaskAccessUpdateRequest request) {
        RolePageTaskAccessDto dto = rolePageTaskAccessManagementService.updateRolePageTaskAccess(request);
        return MessageResponseDTO.<RolePageTaskAccessDto>builder()
                .success(true)
                .message("role.page.task.update.success")
                .data(dto)
                .errors(null)
                .errorCode(0)
                .responseTime(LocalDateTime.now())
                .build();
    }
}
