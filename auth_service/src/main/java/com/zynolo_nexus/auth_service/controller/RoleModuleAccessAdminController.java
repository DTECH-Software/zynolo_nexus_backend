package com.zynolo_nexus.auth_service.controller;

import com.zynolo_nexus.auth_service.dto.api.MessageResponseDTO;
import com.zynolo_nexus.auth_service.enums.RoleCode;
import com.zynolo_nexus.auth_service.exception.BadRequestException;
import com.zynolo_nexus.auth_service.exception.NotFoundException;
import com.zynolo_nexus.auth_service.model.Module;
import com.zynolo_nexus.auth_service.model.Role;
import com.zynolo_nexus.auth_service.model.RoleModuleAccess;
import com.zynolo_nexus.auth_service.repository.ModuleRepository;
import com.zynolo_nexus.auth_service.repository.RoleModuleAccessRepository;
import com.zynolo_nexus.auth_service.repository.RoleRepository;
import com.zynolo_nexus.contracts.modules.RoleModuleAccessDto;
import com.zynolo_nexus.contracts.modules.RoleModuleAccessUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/role-modules")
@RequiredArgsConstructor
public class RoleModuleAccessAdminController {

    private final RoleRepository roleRepository;
    private final ModuleRepository moduleRepository;
    private final RoleModuleAccessRepository accessRepository;

    @GetMapping("/{roleCode}")
    public MessageResponseDTO<RoleModuleAccessDto> getAccess(@PathVariable String roleCode) {
        Role role = resolveRole(roleCode);
        List<RoleModuleAccess> accesses = accessRepository.findByRole(role);
        Map<Long, Boolean> canViewMap = accesses.stream()
                .collect(Collectors.toMap(a -> a.getModule().getId(), RoleModuleAccess::getCanView));

        List<RoleModuleAccessDto.RoleModulePermissionItem> modules = moduleRepository.findAll().stream()
                .map(m -> RoleModuleAccessDto.RoleModulePermissionItem.builder()
                        .moduleCode(m.getCode())
                        .moduleName(m.getName())
                        .canView(canViewMap.getOrDefault(m.getId(), Boolean.FALSE))
                        .build())
                .toList();

        RoleModuleAccessDto dto = RoleModuleAccessDto.builder()
                .roleCode(role.getCode().name())
                .modules(modules)
                .build();

        return MessageResponseDTO.<RoleModuleAccessDto>builder()
                .success(true)
                .message("role.module.fetch.success")
                .data(dto)
                .errors(null)
                .errorCode(0)
                .responseTime(LocalDateTime.now())
                .build();
    }

    @PutMapping
    @Transactional
    public MessageResponseDTO<RoleModuleAccessDto> updateAccess(@RequestBody RoleModuleAccessUpdateRequest request) {
        if (request == null || request.getRoleCode() == null) {
            throw new BadRequestException("role.module.invalid");
        }
        Role role = resolveRole(request.getRoleCode());
        List<Module> modules = moduleRepository.findAll();
        Map<String, Module> moduleByCode = modules.stream()
                .collect(Collectors.toMap(Module::getCode, m -> m));
        Map<Long, RoleModuleAccess> existing = accessRepository.findByRole(role).stream()
                .collect(Collectors.toMap(a -> a.getModule().getId(), a -> a));

        for (RoleModuleAccessUpdateRequest.ModulePermission perm : request.getModules()) {
            Module module = moduleByCode.get(perm.getModuleCode());
            if (module == null) {
                continue;
            }
            RoleModuleAccess access = existing.getOrDefault(module.getId(),
                    RoleModuleAccess.builder()
                            .role(role)
                            .module(module)
                            .canView(false)
                            .build());
            access.setCanView(perm.isCanView());
            accessRepository.save(access);
            existing.put(module.getId(), access);
        }

        return getAccess(role.getCode().name());
    }

    private Role resolveRole(String code) {
        try {
            RoleCode roleCode = RoleCode.valueOf(code);
            return roleRepository.findByCode(roleCode)
                    .orElseThrow(() -> new NotFoundException("role.module.notfound"));
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException("role.module.invalid");
        }
    }
}
