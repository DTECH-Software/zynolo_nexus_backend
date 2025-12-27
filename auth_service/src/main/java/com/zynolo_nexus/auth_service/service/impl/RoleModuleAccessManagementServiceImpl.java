package com.zynolo_nexus.auth_service.service.impl;

import com.zynolo_nexus.auth_service.enums.RoleCode;
import com.zynolo_nexus.auth_service.exception.BadRequestException;
import com.zynolo_nexus.auth_service.exception.NotFoundException;
import com.zynolo_nexus.auth_service.model.Module;
import com.zynolo_nexus.auth_service.model.Role;
import com.zynolo_nexus.auth_service.model.RoleModuleAccess;
import com.zynolo_nexus.auth_service.repository.ModuleRepository;
import com.zynolo_nexus.auth_service.repository.RoleModuleAccessRepository;
import com.zynolo_nexus.auth_service.repository.RoleRepository;
import com.zynolo_nexus.auth_service.service.RoleModuleAccessManagementService;
import com.zynolo_nexus.contracts.modules.RoleModuleAccessDto;
import com.zynolo_nexus.contracts.modules.RoleModuleAccessUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoleModuleAccessManagementServiceImpl implements RoleModuleAccessManagementService {

    private final RoleRepository roleRepository;
    private final ModuleRepository moduleRepository;
    private final RoleModuleAccessRepository roleModuleAccessRepository;

    @Override
    public RoleModuleAccessDto getRoleModuleAccess(String roleCode) {
        Role role = resolveRole(roleCode);

        List<Module> modules = moduleRepository.findAllByActiveTrueOrderBySortOrderAsc();
        List<RoleModuleAccess> existing = roleModuleAccessRepository.findByRole(role);

        Map<Long, RoleModuleAccess> accessMap = existing.stream()
                .collect(Collectors.toMap(a -> a.getModule().getId(), a -> a));

        List<RoleModuleAccessDto.RoleModulePermissionItem> items = modules.stream()
                .map(m -> {
                    RoleModuleAccess access = accessMap.get(m.getId());
                    boolean canView = access != null && Boolean.TRUE.equals(access.getCanView());
                    return RoleModuleAccessDto.RoleModulePermissionItem.builder()
                            .moduleCode(m.getCode())
                            .moduleName(m.getName())
                            .canView(canView)
                            .build();
                })
                .toList();

        return RoleModuleAccessDto.builder()
                .roleCode(roleCode)
                .modules(items)
                .build();
    }

    @Override
    public RoleModuleAccessDto updateRoleModuleAccess(RoleModuleAccessUpdateRequest request) {
        if (request == null || request.getRoleCode() == null) {
            throw new BadRequestException("role.module.invalid");
        }
        Role role = resolveRole(request.getRoleCode());

        List<RoleModuleAccess> existing = roleModuleAccessRepository.findByRole(role);
        roleModuleAccessRepository.deleteAll(existing);

        if (request.getModules() != null) {
            for (RoleModuleAccessUpdateRequest.ModulePermission mp : request.getModules()) {
                if (mp.isCanView()) {
                    Module module = moduleRepository.findByCode(mp.getModuleCode())
                            .orElseThrow(() -> new NotFoundException("module.notfound"));
                    RoleModuleAccess access = RoleModuleAccess.builder()
                            .role(role)
                            .module(module)
                            .canView(true)
                            .build();
                    roleModuleAccessRepository.save(access);
                }
            }
        }

        return getRoleModuleAccess(request.getRoleCode());
    }

    private Role resolveRole(String roleCode) {
        try {
            RoleCode rc = RoleCode.valueOf(roleCode);
            return roleRepository.findByCode(rc)
                    .orElseThrow(() -> new NotFoundException("role.module.notfound"));
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException("role.module.invalid");
        }
    }
}
