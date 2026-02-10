package com.zynolo_nexus.auth_service.service.impl;

import com.zynolo_nexus.auth_service.context.CompanyContext;
import com.zynolo_nexus.auth_service.exception.BadRequestException;
import com.zynolo_nexus.auth_service.exception.NotFoundException;
import com.zynolo_nexus.auth_service.enums.ModuleStatus;
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
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${app.default.company-id:1}")
    private Long defaultCompanyId;

    @Override
    public RoleModuleAccessDto getRoleModuleAccess(String roleCode) {
        Role role = resolveRole(roleCode);
        Long companyId = resolveCompanyId();

        List<Module> modules = moduleRepository.findAllActiveOrderBySortOrderAsc(ModuleStatus.ACTIVE);
        List<RoleModuleAccess> existing = roleModuleAccessRepository.findByRoleAndCompanyId(role, companyId);

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
        Long companyId = resolveCompanyId();

        List<RoleModuleAccess> existing = roleModuleAccessRepository.findByRoleAndCompanyId(role, companyId);
        roleModuleAccessRepository.deleteAll(existing);

        if (request.getModules() != null) {
            for (RoleModuleAccessUpdateRequest.ModulePermission mp : request.getModules()) {
                if (mp.isCanView()) {
                    Module module = moduleRepository.findByCode(mp.getModuleCode())
                            .orElseThrow(() -> new NotFoundException("module.notfound"));
                    RoleModuleAccess access = RoleModuleAccess.builder()
                            .role(role)
                            .module(module)
                            .companyId(companyId)
                            .canView(true)
                            .build();
                    roleModuleAccessRepository.save(access);
                }
            }
        }

        return getRoleModuleAccess(request.getRoleCode());
    }

    private Role resolveRole(String roleCode) {
        if (!org.springframework.util.StringUtils.hasText(roleCode)) {
            throw new BadRequestException("role.module.invalid");
        }
        return roleRepository.findByCodeIgnoreCase(roleCode)
                .orElseThrow(() -> new NotFoundException("role.module.notfound"));
    }

    private Long resolveCompanyId() {
        Long companyId = CompanyContext.getCompanyId();
        return companyId != null ? companyId : defaultCompanyId;
    }
}
