package com.zynolo_nexus.setting_service.service.impl;

import com.zynolo_nexus.contracts.modules.ModuleDto;
import com.zynolo_nexus.contracts.modules.ModuleStatus;
import com.zynolo_nexus.contracts.modules.RoleModuleAccessDto;
import com.zynolo_nexus.contracts.modules.RoleModuleAccessUpdateRequest;
import com.zynolo_nexus.contracts.pages.RolePageTaskAccessDto;
import com.zynolo_nexus.setting_service.client.AuthModuleClient;
import com.zynolo_nexus.setting_service.dto.api.MessageResponseDTO;
import com.zynolo_nexus.setting_service.dto.request.RoleModuleAccessCheckRequest;
import com.zynolo_nexus.setting_service.dto.request.RoleModuleAccessReferenceDataRequest;
import com.zynolo_nexus.setting_service.dto.request.RoleModuleAccessUpdateByIdRequest;
import com.zynolo_nexus.setting_service.dto.request.RoleModuleAccessViewRequest;
import com.zynolo_nexus.setting_service.dto.response.RoleModuleAccessPrivilegesDto;
import com.zynolo_nexus.setting_service.dto.response.RoleModuleAccessReferenceDataDto;
import com.zynolo_nexus.setting_service.dto.response.RoleModuleAccessReferenceModuleDto;
import com.zynolo_nexus.setting_service.dto.response.RoleModuleAccessReferenceRoleDto;
import com.zynolo_nexus.setting_service.dto.response.RoleModulePrivilegeCheckDto;
import com.zynolo_nexus.setting_service.exception.NotFoundException;
import com.zynolo_nexus.setting_service.model.Role;
import com.zynolo_nexus.setting_service.model.User;
import com.zynolo_nexus.setting_service.repository.UserRepository;
import com.zynolo_nexus.setting_service.repository.RoleRepository;
import com.zynolo_nexus.setting_service.service.RoleModuleAccessService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class RoleModuleAccessServiceImpl implements RoleModuleAccessService {

    private static final String ROLE_MODULE_MANAGEMENT_CODE = "URMM";

    private final AuthModuleClient authModuleClient;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;

    @Override
    public MessageResponseDTO<RoleModuleAccessDto> getRoleModuleAccess(String roleCode) {

        RoleModuleAccessDto dto = authModuleClient.getRoleModuleAccess(roleCode);

        return MessageResponseDTO.<RoleModuleAccessDto>builder()
                .success(true)
                .message("Role module access loaded successfully")
                .data(dto)
                .errors(null)
                .errorCode(0)
                .responseTime(LocalDateTime.now())
                .build();
    }

    @Override
    public MessageResponseDTO<RoleModuleAccessDto> updateRoleModuleAccess(RoleModuleAccessUpdateRequest request) {

        RoleModuleAccessDto dto = authModuleClient.updateRoleModuleAccess(request);

        return MessageResponseDTO.<RoleModuleAccessDto>builder()
                .success(true)
                .message("Role module access updated successfully")
                .data(dto)
                .errors(null)
                .errorCode(0)
                .responseTime(LocalDateTime.now())
                .build();
    }

    @Override
    public MessageResponseDTO<RoleModuleAccessDto> getRoleModuleAccess(RoleModuleAccessViewRequest request) {
        String roleCode = resolveRoleCode(request != null ? request.getRoleId() : null,
                request != null ? request.getRoleCode() : null);
        RoleModuleAccessDto dto = authModuleClient.getRoleModuleAccess(roleCode);
        return MessageResponseDTO.<RoleModuleAccessDto>builder()
                .success(true)
                .message("Role module access loaded successfully")
                .data(dto)
                .errors(null)
                .errorCode(0)
                .responseTime(LocalDateTime.now())
                .build();
    }

    @Override
    public MessageResponseDTO<RoleModuleAccessDto> updateRoleModuleAccess(RoleModuleAccessUpdateByIdRequest request) {
        String roleCode = resolveRoleCode(request != null ? request.getRoleId() : null,
                request != null ? request.getRoleCode() : null);

        Map<Long, String> moduleIdMap = loadModuleCodeMap();
        List<RoleModuleAccessUpdateRequest.ModulePermission> modules = request != null && request.getModules() != null
                ? request.getModules().stream()
                .map(module -> {
                    String moduleCode = resolveModuleCode(module, moduleIdMap);
                    RoleModuleAccessUpdateRequest.ModulePermission permission =
                            new RoleModuleAccessUpdateRequest.ModulePermission();
                    permission.setModuleCode(moduleCode);
                    permission.setCanView(module != null && module.isCanView());
                    return permission;
                })
                .toList()
                : List.of();

        RoleModuleAccessUpdateRequest payload = new RoleModuleAccessUpdateRequest();
        payload.setRoleCode(roleCode);
        payload.setModules(modules);

        RoleModuleAccessDto dto = authModuleClient.updateRoleModuleAccess(payload);
        return MessageResponseDTO.<RoleModuleAccessDto>builder()
                .success(true)
                .message("Role module access updated successfully")
                .data(dto)
                .errors(null)
                .errorCode(0)
                .responseTime(LocalDateTime.now())
                .build();
    }

    @Override
    public MessageResponseDTO<RoleModulePrivilegeCheckDto> checkRoleModuleAccess(RoleModuleAccessCheckRequest request) {
        String roleCode = resolveRoleCode(request != null ? request.getRoleId() : null,
                request != null ? request.getRoleCode() : null);

        Map<Long, String> moduleIdMap = loadModuleCodeMap();
        String moduleCode = resolveModuleCode(request, moduleIdMap);

        RoleModuleAccessDto dto = authModuleClient.getRoleModuleAccess(roleCode);
        boolean canView = false;
        if (dto != null && dto.getModules() != null) {
            canView = dto.getModules().stream()
                    .filter(module -> module != null && StringUtils.hasText(module.getModuleCode()))
                    .anyMatch(module -> module.getModuleCode().equalsIgnoreCase(moduleCode)
                            && module.isCanView());
        }

        RoleModulePrivilegeCheckDto response = RoleModulePrivilegeCheckDto.builder()
                .roleId(request != null ? request.getRoleId() : null)
                .roleCode(roleCode)
                .moduleId(request != null ? request.getModuleId() : null)
                .moduleCode(moduleCode)
                .canView(canView)
                .build();

        return MessageResponseDTO.<RoleModulePrivilegeCheckDto>builder()
                .success(true)
                .message("Role module privilege checked successfully")
                .data(response)
                .errors(null)
                .errorCode(0)
                .responseTime(LocalDateTime.now())
                .build();
    }

    @Override
    public MessageResponseDTO<RoleModuleAccessReferenceDataDto> getReferenceData(RoleModuleAccessReferenceDataRequest request) {
        RoleModuleAccessPrivilegesDto privileges = resolvePrivileges(request);

        List<RoleModuleAccessReferenceRoleDto> roles = roleRepository.findAll().stream()
                .filter(role -> role.getStatus() == null || role.getStatus().name().equalsIgnoreCase("ACTIVE"))
                .map(role -> RoleModuleAccessReferenceRoleDto.builder()
                        .id(role.getId())
                        .code(role.getCode())
                        .description(role.getDescription())
                        .build())
                .toList();

        List<RoleModuleAccessReferenceModuleDto> modules = authModuleClient.getAllModulesAll().stream()
                .filter(module -> module != null && (module.getStatus() == null || module.getStatus() == ModuleStatus.ACTIVE))
                .map(module -> RoleModuleAccessReferenceModuleDto.builder()
                        .id(module.getId())
                        .code(module.getCode())
                        .name(module.getName())
                        .description(module.getDescription())
                        .build())
                .toList();

        RoleModuleAccessReferenceDataDto data = RoleModuleAccessReferenceDataDto.builder()
                .roles(roles)
                .modules(modules)
                .privileges(privileges)
                .build();

        return MessageResponseDTO.<RoleModuleAccessReferenceDataDto>builder()
                .success(true)
                .message("Reference data URMM retrieved successfully")
                .data(data)
                .errors(null)
                .errorCode(0)
                .responseTime(LocalDateTime.now())
                .build();
    }

    private String resolveRoleCode(Long roleId, String roleCode) {
        if (roleId != null) {
            Role role = roleRepository.findById(roleId)
                    .orElseThrow(() -> new NotFoundException("role.notfound"));
            return role.getCode();
        }
        if (!StringUtils.hasText(roleCode)) {
            throw new NotFoundException("role.notfound");
        }
        return roleCode;
    }

    private Map<Long, String> loadModuleCodeMap() {
        List<ModuleDto> modules = authModuleClient.getAllModulesAll();
        Map<Long, String> map = new HashMap<>();
        if (modules != null) {
            for (ModuleDto module : modules) {
                if (module != null && module.getId() != null && StringUtils.hasText(module.getCode())) {
                    map.put(module.getId(), module.getCode());
                }
            }
        }
        return map;
    }

    private String resolveModuleCode(RoleModuleAccessUpdateByIdRequest.ModulePermission module,
                                     Map<Long, String> moduleIdMap) {
        if (module != null) {
            if (module.getModuleId() != null) {
                String code = moduleIdMap.get(module.getModuleId());
                if (!StringUtils.hasText(code)) {
                    throw new NotFoundException("module.notfound");
                }
                return code;
            }
            if (StringUtils.hasText(module.getModuleCode())) {
                return module.getModuleCode();
            }
        }
        throw new NotFoundException("module.notfound");
    }

    private String resolveModuleCode(RoleModuleAccessCheckRequest request, Map<Long, String> moduleIdMap) {
        if (request != null && request.getModuleId() != null) {
            String code = moduleIdMap.get(request.getModuleId());
            if (!StringUtils.hasText(code)) {
                throw new NotFoundException("module.notfound");
            }
            return code;
        }
        if (request != null && StringUtils.hasText(request.getModuleCode())) {
            return request.getModuleCode();
        }
        throw new NotFoundException("module.notfound");
    }

    private RoleModuleAccessPrivilegesDto resolvePrivileges(RoleModuleAccessReferenceDataRequest request) {
        String username = request != null ? request.getUsername() : null;
        if (!StringUtils.hasText(username)) {
            username = getAuthenticatedUsername();
        }

        if (!StringUtils.hasText(username)) {
            return RoleModuleAccessPrivilegesDto.builder()
                    .add(false)
                    .update(false)
                    .view(false)
                    .search(false)
                    .delete(false)
                    .build();
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("user.fetch.notfound"));

        String roleCode = user.getRole() != null && user.getRole().getCode() != null
                ? user.getRole().getCode()
                : null;

        if (!StringUtils.hasText(roleCode)) {
            return RoleModuleAccessPrivilegesDto.builder()
                    .add(false)
                    .update(false)
                    .view(false)
                    .search(false)
                    .delete(false)
                    .build();
        }

        RolePageTaskAccessDto access = authModuleClient.getRolePageTaskAccess(roleCode);
        if (access == null || access.getPages() == null) {
            return RoleModuleAccessPrivilegesDto.builder()
                    .add(false)
                    .update(false)
                    .view(false)
                    .search(false)
                    .delete(false)
                    .build();
        }

        Map<String, Boolean> taskAccess = new HashMap<>();
        access.getPages().stream()
                .filter(page -> ROLE_MODULE_MANAGEMENT_CODE.equalsIgnoreCase(page.getPageCode()))
                .findFirst()
                .ifPresent(page -> {
                    if (page.getTasks() != null) {
                        page.getTasks().forEach(task -> {
                            String codeKey = normalizeTaskKey(task.getTaskCode());
                            if (StringUtils.hasText(codeKey)) {
                                taskAccess.put(codeKey, task.isCanAccess());
                            }
                            String nameKey = normalizeTaskKey(task.getTaskName());
                            if (StringUtils.hasText(nameKey)) {
                                taskAccess.putIfAbsent(nameKey, task.isCanAccess());
                            }
                        });
                    }
                });

        boolean add = hasTask(taskAccess, "ADD", "CREATE", "NEW");
        boolean update = hasTask(taskAccess, "UPDATE", "EDIT");
        boolean view = hasTask(taskAccess, "VIEW", "READ");
        boolean search = hasTask(taskAccess, "SEARCH", "FILTER", "LIST");
        boolean delete = hasTask(taskAccess, "DELETE", "REMOVE", "DEACTIVATE");

        return RoleModuleAccessPrivilegesDto.builder()
                .add(add)
                .update(update)
                .view(view)
                .search(search)
                .delete(delete)
                .build();
    }

    private String normalizeTaskKey(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.replaceAll("[^A-Za-z0-9]", "").toUpperCase(Locale.ROOT);
    }

    private boolean hasTask(Map<String, Boolean> taskAccess, String... tokens) {
        if (taskAccess == null || taskAccess.isEmpty() || tokens == null) {
            return false;
        }
        for (Map.Entry<String, Boolean> entry : taskAccess.entrySet()) {
            if (!Boolean.TRUE.equals(entry.getValue())) {
                continue;
            }
            String key = entry.getKey();
            if (!StringUtils.hasText(key)) {
                continue;
            }
            for (String token : tokens) {
                if (StringUtils.hasText(token) && key.contains(token)) {
                    return true;
                }
            }
        }
        return false;
    }

    private String getAuthenticatedUsername() {
        var authentication = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        Object principal = authentication.getPrincipal();
        if (principal == null || "anonymousUser".equals(principal)) {
            return null;
        }
        return authentication.getName();
    }
}
