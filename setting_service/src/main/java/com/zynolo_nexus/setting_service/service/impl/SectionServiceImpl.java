package com.zynolo_nexus.setting_service.service.impl;

import com.zynolo_nexus.contracts.modules.ModuleDto;
import com.zynolo_nexus.contracts.pages.SectionDto;
import com.zynolo_nexus.contracts.pages.SectionRequest;
import com.zynolo_nexus.contracts.pages.SectionStatus;
import com.zynolo_nexus.contracts.pages.SectionStatusRequest;
import com.zynolo_nexus.setting_service.client.AuthModuleClient;
import com.zynolo_nexus.setting_service.dto.api.MessageResponseDTO;
import com.zynolo_nexus.setting_service.dto.request.SectionFilterRequest;
import com.zynolo_nexus.setting_service.dto.request.SectionFilterSearch;
import com.zynolo_nexus.setting_service.dto.request.SectionReferenceDataRequest;
import com.zynolo_nexus.setting_service.dto.request.SectionUpdateRequest;
import com.zynolo_nexus.setting_service.dto.response.ReferenceStatusDto;
import com.zynolo_nexus.setting_service.dto.response.ReferenceModuleDto;
import com.zynolo_nexus.setting_service.dto.response.SectionFilterResultDto;
import com.zynolo_nexus.setting_service.dto.response.SectionListItemDto;
import com.zynolo_nexus.setting_service.dto.response.SectionPrivilegesDto;
import com.zynolo_nexus.setting_service.dto.response.SectionReferenceDataDto;
import com.zynolo_nexus.setting_service.model.User;
import com.zynolo_nexus.setting_service.repository.UserRepository;
import com.zynolo_nexus.setting_service.service.SectionService;
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
public class SectionServiceImpl implements SectionService {

    private static final String SECTION_MANAGEMENT_CODE = "SECM";

    private final AuthModuleClient authModuleClient;
    private final UserRepository userRepository;

    @Override
    public MessageResponseDTO<SectionDto> createSection(SectionRequest request) {
        SectionDto section = authModuleClient.createSection(request);
        return MessageResponseDTO.<SectionDto>builder()
                .success(true)
                .message("Section created successfully")
                .data(section)
                .errors(null)
                .errorCode(0)
                .responseTime(LocalDateTime.now())
                .build();
    }

    @Override
    public MessageResponseDTO<SectionDto> updateSection(String code, SectionRequest request) {
        SectionDto section = authModuleClient.updateSection(code, request);
        return MessageResponseDTO.<SectionDto>builder()
                .success(true)
                .message("Section updated successfully")
                .data(section)
                .errors(null)
                .errorCode(0)
                .responseTime(LocalDateTime.now())
                .build();
    }

    @Override
    public MessageResponseDTO<SectionDto> updateSection(String code, SectionUpdateRequest request) {
        if (request == null) {
            return MessageResponseDTO.<SectionDto>builder()
                    .success(false)
                    .message("Invalid update request")
                    .data(null)
                    .errors(null)
                    .errorCode(400)
                    .responseTime(LocalDateTime.now())
                    .build();
        }

        boolean hasDetails = StringUtils.hasText(request.getModuleCode())
                || StringUtils.hasText(request.getName())
                || StringUtils.hasText(request.getDescription())
                || StringUtils.hasText(request.getUrl())
                || request.getSortOrder() != null;

        SectionDto section;
        if (hasDetails) {
            SectionDto current = findSectionByCode(code);
            SectionRequest update = new SectionRequest();
            update.setCode(code);
            update.setModuleCode(StringUtils.hasText(request.getModuleCode())
                    ? request.getModuleCode()
                    : current.getModuleCode());
            update.setUsername(request.getUsername());
            update.setName(StringUtils.hasText(request.getName()) ? request.getName() : current.getName());
            update.setDescription(StringUtils.hasText(request.getDescription())
                    ? request.getDescription()
                    : current.getDescription());
            update.setUrl(StringUtils.hasText(request.getUrl()) ? request.getUrl() : current.getUrl());
            update.setSortOrder(request.getSortOrder() != null ? request.getSortOrder() : current.getSortOrder());
            section = authModuleClient.updateSection(code, update);
        } else {
            section = findSectionByCode(code);
        }

        if (request.getStatus() != null) {
            SectionStatusRequest statusRequest = new SectionStatusRequest();
            statusRequest.setStatus(request.getStatus());
            statusRequest.setUsername(request.getUsername());
            section = authModuleClient.updateSectionStatus(code, statusRequest);
        }

        return MessageResponseDTO.<SectionDto>builder()
                .success(true)
                .message("Section updated successfully")
                .data(section)
                .errors(null)
                .errorCode(0)
                .responseTime(LocalDateTime.now())
                .build();
    }

    @Override
    public MessageResponseDTO<List<SectionDto>> getAllSections() {
        List<SectionDto> sections = authModuleClient.getAllSections();
        return MessageResponseDTO.<List<SectionDto>>builder()
                .success(true)
                .message("Sections loaded successfully")
                .data(sections)
                .errors(null)
                .errorCode(0)
                .responseTime(LocalDateTime.now())
                .build();
    }

    @Override
    public MessageResponseDTO<SectionDto> getSection(Long id) {
        if (id == null) {
            return MessageResponseDTO.<SectionDto>builder()
                    .success(false)
                    .message("Invalid view request")
                    .data(null)
                    .errors(null)
                    .errorCode(400)
                    .responseTime(LocalDateTime.now())
                    .build();
        }
        SectionDto section = findSectionById(id);
        return MessageResponseDTO.<SectionDto>builder()
                .success(true)
                .message("Section details retrieved successfully")
                .data(section)
                .errors(null)
                .errorCode(0)
                .responseTime(LocalDateTime.now())
                .build();
    }

    @Override
    public MessageResponseDTO<SectionDto> updateSectionStatus(Long id, SectionStatus status, String username) {
        if (id == null) {
            return MessageResponseDTO.<SectionDto>builder()
                    .success(false)
                    .message("Invalid status request")
                    .data(null)
                    .errors(null)
                    .errorCode(400)
                    .responseTime(LocalDateTime.now())
                    .build();
        }
        SectionDto current = findSectionById(id);
        SectionStatusRequest statusRequest = new SectionStatusRequest();
        statusRequest.setStatus(status);
        statusRequest.setUsername(username);
        SectionDto section = authModuleClient.updateSectionStatus(current.getCode(), statusRequest);
        return MessageResponseDTO.<SectionDto>builder()
                .success(true)
                .message("Section status updated successfully")
                .data(section)
                .errors(null)
                .errorCode(0)
                .responseTime(LocalDateTime.now())
                .build();
    }

    @Override
    public MessageResponseDTO<SectionReferenceDataDto> getReferenceData(SectionReferenceDataRequest request) {
        SectionPrivilegesDto privileges = resolvePrivileges(request);
        List<ReferenceModuleDto> modules = authModuleClient.getAllModules().stream()
                .map(module -> ReferenceModuleDto.builder()
                        .code(module.getCode())
                        .description(StringUtils.hasText(module.getDescription())
                                ? module.getDescription()
                                : module.getName())
                        .build())
                .toList();
        SectionReferenceDataDto data = SectionReferenceDataDto.builder()
                .defaultStatus(List.of(
                        ReferenceStatusDto.builder().code("ACTIVE").description("Active").build(),
                        ReferenceStatusDto.builder().code("INACTIVE").description("Inactive").build()
                ))
                .modules(modules)
                .privileges(privileges)
                .build();

        return MessageResponseDTO.<SectionReferenceDataDto>builder()
                .success(true)
                .message("Reference data SECM retrieved successfully")
                .data(data)
                .errors(null)
                .errorCode(0)
                .responseTime(LocalDateTime.now())
                .build();
    }

    @Override
    public MessageResponseDTO<SectionFilterResultDto> filterList(SectionFilterRequest request) {
        List<SectionDto> sections = authModuleClient.getAllSectionsAll();

        SectionFilterSearch search = request != null ? request.getSearch() : null;
        String code = search != null ? normalize(search.getCode()) : null;
        String description = search != null ? normalize(search.getDescription()) : null;
        String status = search != null ? normalize(search.getStatus()) : null;

        List<SectionListItemDto> filtered = sections.stream()
                .filter(section -> matches(code, section.getCode()))
                .filter(section -> matches(description, section.getDescription()))
                .filter(section -> matchesStatus(status, section.isActive()))
                .map(section -> SectionListItemDto.builder()
                        .id(section.getId())
                        .code(section.getCode())
                        .description(StringUtils.hasText(section.getDescription()) ? section.getDescription() : section.getName())
                        .status(section.isActive() ? "ACTIVE" : "INACTIVE")
                        .statusDescription(section.isActive() ? "Active" : "Inactive")
                        .build())
                .toList();

        Comparator<SectionListItemDto> comparator = resolveComparator(
                request != null ? request.getSortColumn() : null,
                request != null ? request.getSortDirection() : null
        );

        List<SectionListItemDto> sorted = filtered.stream().sorted(comparator).toList();

        int size = request != null && request.getSize() != null && request.getSize() > 0 ? request.getSize() : 10;
        int page = request != null && request.getPage() != null && request.getPage() >= 0 ? request.getPage() : 0;
        int totalElements = sorted.size();
        int totalPages = size == 0 ? 1 : (int) Math.ceil((double) totalElements / size);
        int fromIndex = Math.min(page * size, totalElements);
        int toIndex = Math.min(fromIndex + size, totalElements);
        List<SectionListItemDto> pageItems = sorted.subList(fromIndex, toIndex);

        SectionFilterResultDto result = SectionFilterResultDto.builder()
                .items(pageItems)
                .totalRecords(totalElements)
                .totalPages(totalPages)
                .page(page)
                .size(size)
                .build();

        return MessageResponseDTO.<SectionFilterResultDto>builder()
                .success(true)
                .message("Sections filtered successfully")
                .data(result)
                .errors(null)
                .errorCode(0)
                .responseTime(LocalDateTime.now())
                .build();
    }

    @Override
    public MessageResponseDTO<String> deactivateSection(String code) {
        authModuleClient.deactivateSection(code);
        return MessageResponseDTO.<String>builder()
                .success(true)
                .message("Section deactivated successfully")
                .data(code)
                .errors(null)
                .errorCode(0)
                .responseTime(LocalDateTime.now())
                .build();
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

    private boolean matchesStatus(String status, Boolean active) {
        if (!StringUtils.hasText(status)) {
            return true;
        }
        String current = Boolean.TRUE.equals(active) ? "active" : "inactive";
        return current.equals(status);
    }

    private Comparator<SectionListItemDto> resolveComparator(String sortColumn, String sortDirection) {
        String column = normalize(sortColumn);
        Comparator<SectionListItemDto> comparator;
        if ("description".equals(column)) {
            comparator = Comparator.comparing(SectionListItemDto::getDescription, String.CASE_INSENSITIVE_ORDER);
        } else if ("status".equals(column)) {
            comparator = Comparator.comparing(SectionListItemDto::getStatus, String.CASE_INSENSITIVE_ORDER);
        } else {
            comparator = Comparator.comparing(SectionListItemDto::getCode, String.CASE_INSENSITIVE_ORDER);
        }

        String dir = normalize(sortDirection);
        if ("desc".equals(dir)) {
            return comparator.reversed();
        }
        return comparator;
    }

    private SectionDto findSectionById(Long id) {
        List<SectionDto> sections = authModuleClient.getAllSectionsAll();
        return sections.stream()
                .filter(section -> section.getId() != null && section.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new com.zynolo_nexus.setting_service.exception.NotFoundException("section.notfound"));
    }

    private SectionDto findSectionByCode(String code) {
        if (!StringUtils.hasText(code)) {
            throw new com.zynolo_nexus.setting_service.exception.NotFoundException("section.notfound");
        }
        List<SectionDto> sections = authModuleClient.getAllSectionsAll();
        return sections.stream()
                .filter(section -> code.equalsIgnoreCase(section.getCode()))
                .findFirst()
                .orElseThrow(() -> new com.zynolo_nexus.setting_service.exception.NotFoundException("section.notfound"));
    }

    private SectionPrivilegesDto resolvePrivileges(SectionReferenceDataRequest request) {
        String username = request != null ? request.getUsername() : null;
        if (!StringUtils.hasText(username)) {
            username = getAuthenticatedUsername();
        }

        if (!StringUtils.hasText(username)) {
            return SectionPrivilegesDto.builder()
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
                ? user.getRole().getCode()
                : null;

        if (!StringUtils.hasText(roleCode)) {
            return SectionPrivilegesDto.builder()
                    .add(false)
                    .update(false)
                    .view(false)
                    .search(false)
                    .delete(false)
                    .build();
        }

        var access = authModuleClient.getRolePageTaskAccess(roleCode);
        if (access == null || access.getPages() == null) {
            return SectionPrivilegesDto.builder()
                    .add(false)
                    .update(false)
                    .view(false)
                    .search(false)
                    .delete(false)
                    .build();
        }

        Map<String, Boolean> taskAccess = new HashMap<>();
        access.getPages().stream()
                .filter(page -> SECTION_MANAGEMENT_CODE.equalsIgnoreCase(page.getPageCode()))
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

        return SectionPrivilegesDto.builder()
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
