package com.zynolo_nexus.setting_service.service.impl;

import com.zynolo_nexus.contracts.modules.ModuleDto;
import com.zynolo_nexus.contracts.modules.ModuleRequest;
import com.zynolo_nexus.contracts.modules.ModuleStatus;
import com.zynolo_nexus.setting_service.client.AuthModuleClient;
import com.zynolo_nexus.setting_service.dto.api.MessageResponseDTO;
import com.zynolo_nexus.setting_service.dto.request.ModuleFilterRequest;
import com.zynolo_nexus.setting_service.dto.request.ModuleFilterSearch;
import com.zynolo_nexus.setting_service.dto.request.ModuleReferenceDataRequest;
import com.zynolo_nexus.setting_service.dto.request.ModuleUpdateRequest;
import com.zynolo_nexus.setting_service.dto.response.ModuleFilterResultDto;
import com.zynolo_nexus.setting_service.dto.response.ModuleListItemDto;
import com.zynolo_nexus.setting_service.dto.response.ModulePrivilegesDto;
import com.zynolo_nexus.setting_service.dto.response.ModuleReferenceDataDto;
import com.zynolo_nexus.setting_service.dto.response.ReferenceStatusDto;
import com.zynolo_nexus.setting_service.model.User;
import com.zynolo_nexus.setting_service.repository.UserRepository;
import com.zynolo_nexus.setting_service.service.ModuleService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ModuleServiceImpl implements ModuleService {

    private static final String MODULE_MANAGEMENT_CODE = "MODM";

    private final AuthModuleClient authModuleClient;
    private final UserRepository userRepository;

    @Override
    public MessageResponseDTO<ModuleDto> createModule(ModuleRequest request) {
        ModuleDto module = authModuleClient.createModule(request);
        return MessageResponseDTO.<ModuleDto>builder()
                .success(true)
                .message("Module created successfully")
                .data(module)
                .errors(null)
                .errorCode(0)
                .responseTime(LocalDateTime.now())
                .build();
    }

    @Override
    public MessageResponseDTO<ModuleDto> updateModule(String code, ModuleRequest request) {
        ModuleDto module = authModuleClient.updateModule(code, request);
        return MessageResponseDTO.<ModuleDto>builder()
                .success(true)
                .message("Module updated successfully")
                .data(module)
                .errors(null)
                .errorCode(0)
                .responseTime(LocalDateTime.now())
                .build();
    }

    @Override
    public MessageResponseDTO<ModuleDto> updateModule(String code, ModuleUpdateRequest request) {
        if (request == null) {
            return MessageResponseDTO.<ModuleDto>builder()
                    .success(false)
                    .message("Invalid update request")
                    .data(null)
                    .errors(null)
                    .errorCode(400)
                    .responseTime(LocalDateTime.now())
                    .build();
        }

        boolean hasDetails =
                StringUtils.hasText(request.getName()) ||
                StringUtils.hasText(request.getDescription()) ||
                StringUtils.hasText(request.getUrl()) ||
                request.getStatus() != null ||
                request.getSortOrder() != null;

        ModuleDto module;
        if (hasDetails) {
            ModuleDto current = findModuleByCode(code);
            ModuleRequest update = new ModuleRequest();
            update.setCode(code);
            update.setName(StringUtils.hasText(request.getName()) ? request.getName() : current.getName());
            update.setDescription(StringUtils.hasText(request.getDescription())
                    ? request.getDescription()
                    : current.getDescription());
            update.setUrl(StringUtils.hasText(request.getUrl()) ? request.getUrl() : current.getUrl());
            update.setSortOrder(request.getSortOrder() != null ? request.getSortOrder() : current.getSortOrder());
            update.setStatus(request.getStatus() != null ? request.getStatus() : current.getStatus());
            module = authModuleClient.updateModule(code, update);
        } else {
            module = findModuleByCode(code);
        }

        return MessageResponseDTO.<ModuleDto>builder()
                .success(true)
                .message("Module updated successfully")
                .data(module)
                .errors(null)
                .errorCode(0)
                .responseTime(LocalDateTime.now())
                .build();
    }

    @Override
    public MessageResponseDTO<ModuleDto> getModule(Long id) {
        if (id == null) {
            return MessageResponseDTO.<ModuleDto>builder()
                    .success(false)
                    .message("Invalid view request")
                    .data(null)
                    .errors(null)
                    .errorCode(400)
                    .responseTime(LocalDateTime.now())
                    .build();
        }
        ModuleDto module = findModuleById(id);
        return MessageResponseDTO.<ModuleDto>builder()
                .success(true)
                .message("Module details retrieved successfully")
                .data(module)
                .errors(null)
                .errorCode(0)
                .responseTime(LocalDateTime.now())
                .build();
    }

    @Override
    public MessageResponseDTO<ModuleDto> updateModuleStatus(Long id, ModuleStatus status) {
        if (id == null) {
            return MessageResponseDTO.<ModuleDto>builder()
                    .success(false)
                    .message("Invalid status request")
                    .data(null)
                    .errors(null)
                    .errorCode(400)
                    .responseTime(LocalDateTime.now())
                    .build();
        }
        ModuleDto current = findModuleById(id);
        ModuleRequest update = new ModuleRequest();
        update.setStatus(status != null ? status : current.getStatus());
        ModuleDto module = authModuleClient.updateModule(current.getCode(), update);
        return MessageResponseDTO.<ModuleDto>builder()
                .success(true)
                .message("Module status updated successfully")
                .data(module)
                .errors(null)
                .errorCode(0)
                .responseTime(LocalDateTime.now())
                .build();
    }

    @Override
    public MessageResponseDTO<List<ModuleDto>> getAllModules() {
        List<ModuleDto> modules = authModuleClient.getAllModules();
        return MessageResponseDTO.<List<ModuleDto>>builder()
                .success(true)
                .message("Modules loaded successfully")
                .data(modules)
                .errors(null)
                .errorCode(0)
                .responseTime(LocalDateTime.now())
                .build();
    }

    @Override
    public MessageResponseDTO<ModuleReferenceDataDto> getReferenceData(ModuleReferenceDataRequest request) {
        ModulePrivilegesDto privileges = resolvePrivileges(request);
        ModuleReferenceDataDto data = ModuleReferenceDataDto.builder()
                .defaultStatus(List.of(
                        ReferenceStatusDto.builder().code("ACTIVE").description("Active").build(),
                        ReferenceStatusDto.builder().code("DEACTIVE").description("Deactive").build()
                ))
                .privileges(privileges)
                .build();

        return MessageResponseDTO.<ModuleReferenceDataDto>builder()
                .success(true)
                .message("Reference data MODM retrieved successfully")
                .data(data)
                .errors(null)
                .errorCode(0)
                .responseTime(LocalDateTime.now())
                .build();
    }

    @Override
    public MessageResponseDTO<ModuleFilterResultDto> filterList(ModuleFilterRequest request) {
        List<ModuleDto> modules = authModuleClient.getAllModulesAll();

        ModuleFilterSearch search = request != null ? request.getSearch() : null;
        String code = search != null ? normalize(search.getCode()) : null;
        String description = search != null ? normalize(search.getDescription()) : null;
        String status = search != null ? normalize(search.getStatus()) : null;

        List<ModuleListItemDto> filtered = modules.stream()
                .filter(module -> matches(code, module.getCode()))
                .filter(module -> matches(description, module.getDescription()))
                .filter(module -> matchesStatus(status, module.getStatus()))
                .map(module -> ModuleListItemDto.builder()
                        .id(module.getId())
                        .code(module.getCode())
                        .description(StringUtils.hasText(module.getDescription()) ? module.getDescription() : module.getName())
                        .status(module.getStatus() != null ? module.getStatus().name() : "ACTIVE")
                        .statusDescription(module.getStatus() == ModuleStatus.DEACTIVE ? "Deactive" : "Active")
                        .build())
                .toList();

        Comparator<ModuleListItemDto> comparator = resolveComparator(
                request != null ? request.getSortColumn() : null,
                request != null ? request.getSortDirection() : null
        );

        List<ModuleListItemDto> sorted = filtered.stream().sorted(comparator).toList();

        int size = request != null && request.getSize() != null && request.getSize() > 0 ? request.getSize() : 10;
        int page = request != null && request.getPage() != null && request.getPage() >= 0 ? request.getPage() : 0;
        int totalElements = sorted.size();
        int totalPages = size == 0 ? 1 : (int) Math.ceil((double) totalElements / size);
        int fromIndex = Math.min(page * size, totalElements);
        int toIndex = Math.min(fromIndex + size, totalElements);
        List<ModuleListItemDto> pageItems = sorted.subList(fromIndex, toIndex);

        ModuleFilterResultDto result = ModuleFilterResultDto.builder()
                .items(pageItems)
                .totalRecords(totalElements)
                .totalPages(totalPages)
                .page(page)
                .size(size)
                .build();

        return MessageResponseDTO.<ModuleFilterResultDto>builder()
                .success(true)
                .message("Modules filtered successfully")
                .data(result)
                .errors(null)
                .errorCode(0)
                .responseTime(LocalDateTime.now())
                .build();
    }

    @Override
    public MessageResponseDTO<String> deactivateModule(String code) {
        authModuleClient.deactivateModule(code);
        return MessageResponseDTO.<String>builder()
                .success(true)
                .message("Module deactivated successfully")
                .data(code)
                .errors(null)
                .errorCode(0)
                .responseTime(LocalDateTime.now())
                .build();
    }

    private ModuleDto findModuleById(Long id) {
        List<ModuleDto> modules = authModuleClient.getAllModulesAll();
        return modules.stream()
                .filter(module -> module.getId() != null && module.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new com.zynolo_nexus.setting_service.exception.NotFoundException("module.notfound"));
    }

    private ModuleDto findModuleByCode(String code) {
        if (!StringUtils.hasText(code)) {
            throw new com.zynolo_nexus.setting_service.exception.NotFoundException("module.notfound");
        }
        List<ModuleDto> modules = authModuleClient.getAllModulesAll();
        return modules.stream()
                .filter(module -> code.equalsIgnoreCase(module.getCode()))
                .findFirst()
                .orElseThrow(() -> new com.zynolo_nexus.setting_service.exception.NotFoundException("module.notfound"));
    }

    private String normalize(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim().toLowerCase(Locale.ROOT);
    }

    private boolean matches(String searchValue, String actual) {
        if (!StringUtils.hasText(searchValue)) {
            return true;
        }
        return actual != null && actual.toLowerCase(Locale.ROOT).contains(searchValue);
    }

    private boolean matchesStatus(String status, ModuleStatus moduleStatus) {
        if (!StringUtils.hasText(status)) {
            return true;
        }
        String current = moduleStatus == ModuleStatus.DEACTIVE ? "deactive" : "active";
        return current.equals(status);
    }

    private Comparator<ModuleListItemDto> resolveComparator(String sortColumn, String sortDirection) {
        String column = normalize(sortColumn);
        Comparator<ModuleListItemDto> comparator;
        if ("description".equals(column)) {
            comparator = Comparator.comparing(ModuleListItemDto::getDescription, String.CASE_INSENSITIVE_ORDER);
        } else if ("status".equals(column)) {
            comparator = Comparator.comparing(ModuleListItemDto::getStatus, String.CASE_INSENSITIVE_ORDER);
        } else {
            comparator = Comparator.comparing(ModuleListItemDto::getCode, String.CASE_INSENSITIVE_ORDER);
        }

        String dir = normalize(sortDirection);
        if ("desc".equals(dir)) {
            return comparator.reversed();
        }
        return comparator;
    }

    private ModulePrivilegesDto resolvePrivileges(ModuleReferenceDataRequest request) {
        String username = request != null ? request.getUsername() : null;
        if (!StringUtils.hasText(username)) {
            username = getAuthenticatedUsername();
        }

        if (!StringUtils.hasText(username)) {
            return ModulePrivilegesDto.builder()
                    .add(false)
                    .update(false)
                    .view(false)
                    .search(false)
                    .delete(false)
                    .build();
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new com.zynolo_nexus.setting_service.exception.NotFoundException("user.fetch.notfound"));

        String roleCode = user.getRole() != null && user.getRole().getCode() != null
                ? user.getRole().getCode().name()
                : null;

        if (!StringUtils.hasText(roleCode)) {
            return ModulePrivilegesDto.builder()
                    .add(false)
                    .update(false)
                    .view(false)
                    .search(false)
                    .delete(false)
                    .build();
        }

        var access = authModuleClient.getRolePageTaskAccess(roleCode);
        if (access == null || access.getPages() == null) {
            return ModulePrivilegesDto.builder()
                    .add(false)
                    .update(false)
                    .view(false)
                    .search(false)
                    .delete(false)
                    .build();
        }

        Map<String, Boolean> taskAccess = new HashMap<>();
        access.getPages().stream()
                .filter(page -> MODULE_MANAGEMENT_CODE.equalsIgnoreCase(page.getPageCode()))
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

        return ModulePrivilegesDto.builder()
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
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
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
