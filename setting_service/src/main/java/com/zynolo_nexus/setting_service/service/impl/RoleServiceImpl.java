package com.zynolo_nexus.setting_service.service.impl;

import com.zynolo_nexus.setting_service.client.AuthModuleClient;
import com.zynolo_nexus.setting_service.dto.api.MessageResponseDTO;
import com.zynolo_nexus.setting_service.dto.request.RoleCreateRequest;
import com.zynolo_nexus.setting_service.dto.request.RoleDeleteRequest;
import com.zynolo_nexus.setting_service.dto.request.RoleFilterRequest;
import com.zynolo_nexus.setting_service.dto.request.RoleFilterSearch;
import com.zynolo_nexus.setting_service.dto.request.RoleReferenceDataRequest;
import com.zynolo_nexus.setting_service.dto.request.RoleStatusUpdateRequest;
import com.zynolo_nexus.setting_service.dto.request.RoleUpdateRequest;
import com.zynolo_nexus.setting_service.dto.response.ReferenceStatusDto;
import com.zynolo_nexus.setting_service.dto.response.RoleDto;
import com.zynolo_nexus.setting_service.dto.response.RoleFilterResultDto;
import com.zynolo_nexus.setting_service.dto.response.RoleListItemDto;
import com.zynolo_nexus.setting_service.dto.response.RolePrivilegesDto;
import com.zynolo_nexus.setting_service.dto.response.RoleReferenceDataDto;
import com.zynolo_nexus.setting_service.enums.RoleStatus;
import com.zynolo_nexus.setting_service.exception.BadRequestException;
import com.zynolo_nexus.setting_service.exception.NotFoundException;
import com.zynolo_nexus.setting_service.model.Role;
import com.zynolo_nexus.setting_service.model.User;
import com.zynolo_nexus.setting_service.repository.RoleRepository;
import com.zynolo_nexus.setting_service.repository.UserRepository;
import com.zynolo_nexus.setting_service.service.RoleService;
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
public class RoleServiceImpl implements RoleService {

    private static final String ROLE_MANAGEMENT_CODE = "USRM";

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final AuthModuleClient authModuleClient;

    @Override
    public MessageResponseDTO<RoleDto> createRole(RoleCreateRequest request) {
        if (request == null || !StringUtils.hasText(request.getCode()) || !StringUtils.hasText(request.getDescription())) {
            throw new BadRequestException("role.create.invalid");
        }

        String code = normalizeCode(request.getCode());
        if (roleRepository.existsByCodeIgnoreCase(code)) {
            throw new BadRequestException("role.code.exists");
        }

        RoleStatus status = request.getStatus() != null ? request.getStatus() : RoleStatus.ACTIVE;

        Role role = Role.builder()
                .code(code)
                .description(request.getDescription().trim())
                .status(status)
                .build();

        role = roleRepository.save(role);

        return MessageResponseDTO.<RoleDto>builder()
                .success(true)
                .message("User role created successfully")
                .data(toDto(role))
                .errors(null)
                .errorCode(0)
                .responseTime(LocalDateTime.now())
                .build();
    }

    @Override
    public MessageResponseDTO<RoleDto> updateRole(RoleUpdateRequest request) {
        if (request == null || request.getId() == null) {
            return MessageResponseDTO.<RoleDto>builder()
                    .success(false)
                    .message("Invalid update request")
                    .data(null)
                    .errors(null)
                    .errorCode(400)
                    .responseTime(LocalDateTime.now())
                    .build();
        }

        Role role = roleRepository.findById(request.getId())
                .orElseThrow(() -> new NotFoundException("role.notfound"));

        if (StringUtils.hasText(request.getCode())) {
            String code = normalizeCode(request.getCode());
            if (!code.equalsIgnoreCase(role.getCode())
                    && roleRepository.existsByCodeIgnoreCase(code)) {
                throw new BadRequestException("role.code.exists");
            }
            role.setCode(code);
        }

        if (StringUtils.hasText(request.getDescription())) {
            role.setDescription(request.getDescription().trim());
        }

        if (request.getStatus() != null) {
            role.setStatus(request.getStatus());
        }

        role = roleRepository.save(role);

        return MessageResponseDTO.<RoleDto>builder()
                .success(true)
                .message("User role updated successfully")
                .data(toDto(role))
                .errors(null)
                .errorCode(0)
                .responseTime(LocalDateTime.now())
                .build();
    }

    @Override
    public MessageResponseDTO<RoleDto> viewRole(Long id) {
        if (id == null) {
            return MessageResponseDTO.<RoleDto>builder()
                    .success(false)
                    .message("Invalid view request")
                    .data(null)
                    .errors(null)
                    .errorCode(400)
                    .responseTime(LocalDateTime.now())
                    .build();
        }

        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("role.notfound"));

        return MessageResponseDTO.<RoleDto>builder()
                .success(true)
                .message("User role found with ID: " + id)
                .data(toDto(role))
                .errors(null)
                .errorCode(0)
                .responseTime(LocalDateTime.now())
                .build();
    }

    @Override
    public MessageResponseDTO<RoleDto> updateRoleStatus(RoleStatusUpdateRequest request) {
        if (request == null || request.getId() == null) {
            return MessageResponseDTO.<RoleDto>builder()
                    .success(false)
                    .message("Invalid status request")
                    .data(null)
                    .errors(null)
                    .errorCode(400)
                    .responseTime(LocalDateTime.now())
                    .build();
        }

        Role role = roleRepository.findById(request.getId())
                .orElseThrow(() -> new NotFoundException("role.notfound"));

        RoleStatus status = request.getStatus() != null ? request.getStatus() : role.getStatus();
        role.setStatus(status != null ? status : RoleStatus.ACTIVE);
        role = roleRepository.save(role);

        return MessageResponseDTO.<RoleDto>builder()
                .success(true)
                .message("User role status updated successfully")
                .data(toDto(role))
                .errors(null)
                .errorCode(0)
                .responseTime(LocalDateTime.now())
                .build();
    }

    @Override
    public MessageResponseDTO<String> deleteRole(RoleDeleteRequest request) {
        if (request == null || request.getId() == null) {
            return MessageResponseDTO.<String>builder()
                    .success(false)
                    .message("Invalid delete request")
                    .data(null)
                    .errors(null)
                    .errorCode(400)
                    .responseTime(LocalDateTime.now())
                    .build();
        }

        Role role = roleRepository.findById(request.getId())
                .orElseThrow(() -> new NotFoundException("role.notfound"));
        roleRepository.delete(role);

        return MessageResponseDTO.<String>builder()
                .success(true)
                .message("User role deleted successfully")
                .data(String.valueOf(request.getId()))
                .errors(null)
                .errorCode(0)
                .responseTime(LocalDateTime.now())
                .build();
    }

    @Override
    public MessageResponseDTO<RoleFilterResultDto> filterList(RoleFilterRequest request) {
        List<Role> roles = roleRepository.findAll();

        RoleFilterSearch search = request != null ? request.getSearch() : null;
        String code = search != null ? normalize(search.getCode()) : null;
        String description = search != null ? normalize(search.getDescription()) : null;
        String status = search != null ? normalize(search.getStatus()) : null;

        List<RoleListItemDto> filtered = roles.stream()
                .filter(role -> matches(code, role.getCode()))
                .filter(role -> matches(description, role.getDescription()))
                .filter(role -> matchesStatus(status, role.getStatus()))
                .map(role -> {
                    RoleStatus current = role.getStatus() != null ? role.getStatus() : RoleStatus.ACTIVE;
                    return RoleListItemDto.builder()
                            .id(role.getId())
                            .code(role.getCode())
                            .description(role.getDescription())
                            .status(current.name())
                            .statusDescription(current == RoleStatus.INACTIVE ? "Inactive" : "Active")
                            .createdDate(role.getCreatedDate())
                            .lastModifiedDate(role.getLastModifiedDate())
                            .createdBy(role.getCreatedBy())
                            .lastModifiedBy(role.getLastModifiedBy())
                            .build();
                })
                .toList();

        Comparator<RoleListItemDto> comparator = resolveComparator(
                request != null ? request.getSortColumn() : null,
                request != null ? request.getSortDirection() : null
        );

        List<RoleListItemDto> sorted = filtered.stream().sorted(comparator).toList();

        int size = request != null && request.getSize() != null && request.getSize() > 0 ? request.getSize() : 10;
        int page = request != null && request.getPage() != null && request.getPage() >= 0 ? request.getPage() : 0;
        int totalElements = sorted.size();
        int totalPages = size == 0 ? 1 : (int) Math.ceil((double) totalElements / size);
        int fromIndex = Math.min(page * size, totalElements);
        int toIndex = Math.min(fromIndex + size, totalElements);
        List<RoleListItemDto> pageItems = sorted.subList(fromIndex, toIndex);

        RoleFilterResultDto result = RoleFilterResultDto.builder()
                .content(pageItems)
                .totalRecords(totalElements)
                .totalPages(totalPages)
                .page(page)
                .size(size)
                .build();

        return MessageResponseDTO.<RoleFilterResultDto>builder()
                .success(true)
                .message("User roles filtered successfully")
                .data(result)
                .errors(null)
                .errorCode(0)
                .responseTime(LocalDateTime.now())
                .build();
    }

    @Override
    public MessageResponseDTO<RoleReferenceDataDto> getReferenceData(RoleReferenceDataRequest request) {
        RolePrivilegesDto privileges = resolvePrivileges(request);
        RoleReferenceDataDto data = RoleReferenceDataDto.builder()
                .defaultStatus(List.of(
                        ReferenceStatusDto.builder().code("ACTIVE").description("Active").build(),
                        ReferenceStatusDto.builder().code("INACTIVE").description("Inactive").build()
                ))
                .privileges(privileges)
                .build();

        return MessageResponseDTO.<RoleReferenceDataDto>builder()
                .success(true)
                .message("Reference data USRM retrieved successfully")
                .data(data)
                .errors(null)
                .errorCode(0)
                .responseTime(LocalDateTime.now())
                .build();
    }

    private RoleDto toDto(Role role) {
        RoleStatus current = role.getStatus() != null ? role.getStatus() : RoleStatus.ACTIVE;
        return RoleDto.builder()
                .id(role.getId())
                .code(role.getCode())
                .description(role.getDescription())
                .status(current.name())
                .statusDescription(current == RoleStatus.INACTIVE ? "Inactive" : "Active")
                .createdDate(role.getCreatedDate())
                .lastModifiedDate(role.getLastModifiedDate())
                .createdBy(role.getCreatedBy())
                .lastModifiedBy(role.getLastModifiedBy())
                .build();
    }

    private String normalizeCode(String value) {
        return value != null ? value.trim() : null;
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

    private boolean matchesStatus(String status, RoleStatus roleStatus) {
        if (!StringUtils.hasText(status)) {
            return true;
        }
        String current = roleStatus == RoleStatus.INACTIVE ? "inactive" : "active";
        return current.equals(status);
    }

    private Comparator<RoleListItemDto> resolveComparator(String sortColumn, String sortDirection) {
        String column = normalize(sortColumn);
        Comparator<RoleListItemDto> comparator;
        if ("description".equals(column)) {
            comparator = Comparator.comparing(RoleListItemDto::getDescription, String.CASE_INSENSITIVE_ORDER);
        } else if ("status".equals(column)) {
            comparator = Comparator.comparing(RoleListItemDto::getStatus, String.CASE_INSENSITIVE_ORDER);
        } else if ("createddate".equals(column)) {
            comparator = Comparator.comparing(RoleListItemDto::getCreatedDate, Comparator.nullsLast(Comparator.naturalOrder()));
        } else if ("lastmodifieddate".equals(column)) {
            comparator = Comparator.comparing(RoleListItemDto::getLastModifiedDate, Comparator.nullsLast(Comparator.naturalOrder()));
        } else {
            comparator = Comparator.comparing(RoleListItemDto::getCode, String.CASE_INSENSITIVE_ORDER);
        }

        String dir = normalize(sortDirection);
        if ("desc".equals(dir)) {
            return comparator.reversed();
        }
        return comparator;
    }

    private RolePrivilegesDto resolvePrivileges(RoleReferenceDataRequest request) {
        String username = request != null ? request.getUsername() : null;
        if (!StringUtils.hasText(username)) {
            username = getAuthenticatedUsername();
        }

        if (!StringUtils.hasText(username)) {
            return RolePrivilegesDto.builder()
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
            return RolePrivilegesDto.builder()
                    .add(false)
                    .update(false)
                    .view(false)
                    .search(false)
                    .delete(false)
                    .build();
        }

        var access = authModuleClient.getRolePageTaskAccess(roleCode);
        if (access == null || access.getPages() == null) {
            return RolePrivilegesDto.builder()
                    .add(false)
                    .update(false)
                    .view(false)
                    .search(false)
                    .delete(false)
                    .build();
        }

        Map<String, Boolean> taskAccess = new HashMap<>();
        access.getPages().stream()
                .filter(page -> ROLE_MANAGEMENT_CODE.equalsIgnoreCase(page.getPageCode()))
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

        return RolePrivilegesDto.builder()
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
