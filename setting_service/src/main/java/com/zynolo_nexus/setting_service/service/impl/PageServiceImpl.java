package com.zynolo_nexus.setting_service.service.impl;

import com.zynolo_nexus.contracts.pages.PageDto;
import com.zynolo_nexus.contracts.pages.PageRequest;
import com.zynolo_nexus.contracts.pages.PageStatus;
import com.zynolo_nexus.contracts.pages.PageStatusRequest;
import com.zynolo_nexus.setting_service.client.AuthModuleClient;
import com.zynolo_nexus.setting_service.dto.api.MessageResponseDTO;
import com.zynolo_nexus.setting_service.dto.request.PageFilterRequest;
import com.zynolo_nexus.setting_service.dto.request.PageFilterSearch;
import com.zynolo_nexus.setting_service.dto.request.PageReferenceDataRequest;
import com.zynolo_nexus.setting_service.dto.request.PageUpdateRequest;
import com.zynolo_nexus.setting_service.dto.response.PageFilterResultDto;
import com.zynolo_nexus.setting_service.dto.response.PageListItemDto;
import com.zynolo_nexus.setting_service.dto.response.PagePrivilegesDto;
import com.zynolo_nexus.setting_service.dto.response.PageReferenceDataDto;
import com.zynolo_nexus.setting_service.dto.response.ReferenceStatusDto;
import com.zynolo_nexus.setting_service.model.User;
import com.zynolo_nexus.setting_service.repository.UserRepository;
import com.zynolo_nexus.setting_service.service.PageService;
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
public class PageServiceImpl implements PageService {

    private static final String PAGE_MANAGEMENT_CODE = "PAGM";

    private final AuthModuleClient authModuleClient;
    private final UserRepository userRepository;

    @Override
    public MessageResponseDTO<PageDto> createPage(PageRequest request) {
        PageDto page = authModuleClient.createPage(request);
        return MessageResponseDTO.<PageDto>builder()
                .success(true)
                .message("Page created successfully")
                .data(page)
                .errors(null)
                .errorCode(0)
                .responseTime(LocalDateTime.now())
                .build();
    }

    @Override
    public MessageResponseDTO<PageDto> updatePage(String code, PageUpdateRequest request) {
        if (request == null) {
            return MessageResponseDTO.<PageDto>builder()
                    .success(false)
                    .message("Invalid update request")
                    .data(null)
                    .errors(null)
                    .errorCode(400)
                    .responseTime(LocalDateTime.now())
                    .build();
        }

        boolean hasDetails = StringUtils.hasText(request.getSectionCode())
                || StringUtils.hasText(request.getName())
                || StringUtils.hasText(request.getDescription())
                || StringUtils.hasText(request.getUrl())
                || request.getSortOrder() != null;

        PageDto page;
        if (hasDetails) {
            PageDto current = authModuleClient.getPage(code);
            PageRequest update = new PageRequest();
            update.setCode(code);
            update.setSectionCode(StringUtils.hasText(request.getSectionCode())
                    ? request.getSectionCode()
                    : current.getSectionCode());
            update.setName(StringUtils.hasText(request.getName()) ? request.getName() : current.getName());
            update.setDescription(StringUtils.hasText(request.getDescription())
                    ? request.getDescription()
                    : current.getDescription());
            update.setUrl(StringUtils.hasText(request.getUrl()) ? request.getUrl() : current.getUrl());
            update.setSortOrder(request.getSortOrder() != null ? request.getSortOrder() : current.getSortOrder());
            page = authModuleClient.updatePage(code, update);
        } else {
            page = authModuleClient.getPage(code);
        }

        if (request.getStatus() != null) {
            PageStatusRequest statusRequest = new PageStatusRequest();
            statusRequest.setStatus(request.getStatus());
            page = authModuleClient.updatePageStatus(code, statusRequest);
        }

        return MessageResponseDTO.<PageDto>builder()
                .success(true)
                .message("Page updated successfully")
                .data(page)
                .errors(null)
                .errorCode(0)
                .responseTime(LocalDateTime.now())
                .build();
    }

    @Override
    public MessageResponseDTO<List<PageDto>> getAllPages() {
        List<PageDto> pages = authModuleClient.getAllPages();
        return MessageResponseDTO.<List<PageDto>>builder()
                .success(true)
                .message("Pages loaded successfully")
                .data(pages)
                .errors(null)
                .errorCode(0)
                .responseTime(LocalDateTime.now())
                .build();
    }

    @Override
    public MessageResponseDTO<PageDto> getPage(String code) {
        PageDto page = authModuleClient.getPage(code);
        return MessageResponseDTO.<PageDto>builder()
                .success(true)
                .message("Page details retrieved successfully")
                .data(page)
                .errors(null)
                .errorCode(0)
                .responseTime(LocalDateTime.now())
                .build();
    }

    @Override
    public MessageResponseDTO<PageDto> updatePageStatus(String code, PageStatus status) {
        PageStatusRequest statusRequest = new PageStatusRequest();
        statusRequest.setStatus(status);
        PageDto page = authModuleClient.updatePageStatus(code, statusRequest);
        return MessageResponseDTO.<PageDto>builder()
                .success(true)
                .message("Page status updated successfully")
                .data(page)
                .errors(null)
                .errorCode(0)
                .responseTime(LocalDateTime.now())
                .build();
    }

    @Override
    public MessageResponseDTO<PageReferenceDataDto> getReferenceData(PageReferenceDataRequest request) {
        PagePrivilegesDto privileges = resolvePrivileges(request);
        PageReferenceDataDto data = PageReferenceDataDto.builder()
                .defaultStatus(List.of(
                        ReferenceStatusDto.builder().code("ACTIVE").description("Active").build(),
                        ReferenceStatusDto.builder().code("INACTIVE").description("Inactive").build()
                ))
                .privileges(privileges)
                .build();

        return MessageResponseDTO.<PageReferenceDataDto>builder()
                .success(true)
                .message("Reference data PAGM retrieved successfully")
                .data(data)
                .errors(null)
                .errorCode(0)
                .responseTime(LocalDateTime.now())
                .build();
    }

    @Override
    public MessageResponseDTO<PageFilterResultDto> filterList(PageFilterRequest request) {
        List<PageDto> pages = authModuleClient.getAllPagesAll();

        PageFilterSearch search = request != null ? request.getSearch() : null;
        String code = search != null ? normalize(search.getCode()) : null;
        String description = search != null ? normalize(search.getDescription()) : null;
        String status = search != null ? normalize(search.getStatus()) : null;

        List<PageListItemDto> filtered = pages.stream()
                .filter(page -> matches(code, page.getCode()))
                .filter(page -> matches(description, page.getDescription()))
                .filter(page -> matchesStatus(status, page.isActive()))
                .map(page -> PageListItemDto.builder()
                        .code(page.getCode())
                        .description(StringUtils.hasText(page.getDescription()) ? page.getDescription() : page.getName())
                        .status(page.isActive() ? "ACTIVE" : "INACTIVE")
                        .build())
                .toList();

        Comparator<PageListItemDto> comparator = resolveComparator(
                request != null ? request.getSortColumn() : null,
                request != null ? request.getSortDirection() : null
        );

        List<PageListItemDto> sorted = filtered.stream().sorted(comparator).toList();

        int size = request != null && request.getSize() != null && request.getSize() > 0 ? request.getSize() : 10;
        int page = request != null && request.getPage() != null && request.getPage() >= 0 ? request.getPage() : 0;
        int totalElements = sorted.size();
        int totalPages = size == 0 ? 1 : (int) Math.ceil((double) totalElements / size);
        int fromIndex = Math.min(page * size, totalElements);
        int toIndex = Math.min(fromIndex + size, totalElements);
        List<PageListItemDto> pageItems = sorted.subList(fromIndex, toIndex);

        PageFilterResultDto result = PageFilterResultDto.builder()
                .items(pageItems)
                .totalElements(totalElements)
                .totalPages(totalPages)
                .page(page)
                .size(size)
                .build();

        return MessageResponseDTO.<PageFilterResultDto>builder()
                .success(true)
                .message("Pages filtered successfully")
                .data(result)
                .errors(null)
                .errorCode(0)
                .responseTime(LocalDateTime.now())
                .build();
    }

    @Override
    public MessageResponseDTO<String> deactivatePage(String code) {
        authModuleClient.deactivatePage(code);
        return MessageResponseDTO.<String>builder()
                .success(true)
                .message("Page deactivated successfully")
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

    private Comparator<PageListItemDto> resolveComparator(String sortColumn, String sortDirection) {
        String column = normalize(sortColumn);
        Comparator<PageListItemDto> comparator;
        if ("description".equals(column)) {
            comparator = Comparator.comparing(PageListItemDto::getDescription, String.CASE_INSENSITIVE_ORDER);
        } else if ("status".equals(column)) {
            comparator = Comparator.comparing(PageListItemDto::getStatus, String.CASE_INSENSITIVE_ORDER);
        } else {
            comparator = Comparator.comparing(PageListItemDto::getCode, String.CASE_INSENSITIVE_ORDER);
        }

        String dir = normalize(sortDirection);
        if ("desc".equals(dir)) {
            return comparator.reversed();
        }
        return comparator;
    }

    private PagePrivilegesDto resolvePrivileges(PageReferenceDataRequest request) {
        String username = request != null ? request.getUsername() : null;
        if (!StringUtils.hasText(username)) {
            username = getAuthenticatedUsername();
        }

        if (!StringUtils.hasText(username)) {
            return PagePrivilegesDto.builder()
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
            return PagePrivilegesDto.builder()
                    .add(false)
                    .update(false)
                    .view(false)
                    .search(false)
                    .delete(false)
                    .build();
        }

        var access = authModuleClient.getRolePageTaskAccess(roleCode);
        if (access == null || access.getPages() == null) {
            return PagePrivilegesDto.builder()
                    .add(false)
                    .update(false)
                    .view(false)
                    .search(false)
                    .delete(false)
                    .build();
        }

        Map<String, Boolean> taskAccess = new HashMap<>();
        access.getPages().stream()
                .filter(page -> PAGE_MANAGEMENT_CODE.equalsIgnoreCase(page.getPageCode()))
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

        return PagePrivilegesDto.builder()
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
