package com.zynolo_nexus.setting_service.service.impl;

import com.zynolo_nexus.contracts.pages.PageStatus;
import com.zynolo_nexus.contracts.pages.TaskDto;
import com.zynolo_nexus.contracts.pages.TaskRequest;
import com.zynolo_nexus.setting_service.client.AuthModuleClient;
import com.zynolo_nexus.setting_service.dto.api.MessageResponseDTO;
import com.zynolo_nexus.setting_service.dto.request.TaskFilterRequest;
import com.zynolo_nexus.setting_service.dto.request.TaskFilterSearch;
import com.zynolo_nexus.setting_service.dto.request.TaskReferenceDataRequest;
import com.zynolo_nexus.setting_service.dto.request.TaskUpdateRequest;
import com.zynolo_nexus.setting_service.dto.response.ReferenceStatusDto;
import com.zynolo_nexus.setting_service.dto.response.TaskFilterResultDto;
import com.zynolo_nexus.setting_service.dto.response.TaskListItemDto;
import com.zynolo_nexus.setting_service.dto.response.TaskPrivilegesDto;
import com.zynolo_nexus.setting_service.dto.response.TaskReferenceDataDto;
import com.zynolo_nexus.setting_service.model.User;
import com.zynolo_nexus.setting_service.repository.UserRepository;
import com.zynolo_nexus.setting_service.service.TaskService;
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
public class TaskServiceImpl implements TaskService {

    private static final String TASK_MANAGEMENT_CODE = "TASM";

    private final AuthModuleClient authModuleClient;
    private final UserRepository userRepository;

    @Override
    public MessageResponseDTO<TaskDto> createTask(TaskRequest request) {
        TaskDto task = authModuleClient.createTask(request);
        return MessageResponseDTO.<TaskDto>builder()
                .success(true)
                .message("Task created successfully")
                .data(task)
                .errors(null)
                .errorCode(0)
                .responseTime(LocalDateTime.now())
                .build();
    }

    @Override
    public MessageResponseDTO<TaskDto> updateTask(String code, TaskRequest request) {
        TaskDto task = authModuleClient.updateTask(code, request);
        return MessageResponseDTO.<TaskDto>builder()
                .success(true)
                .message("Task updated successfully")
                .data(task)
                .errors(null)
                .errorCode(0)
                .responseTime(LocalDateTime.now())
                .build();
    }

    @Override
    public MessageResponseDTO<TaskDto> updateTask(String code, TaskUpdateRequest request) {
        if (request == null) {
            return MessageResponseDTO.<TaskDto>builder()
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
                request.getSortOrder() != null ||
                request.getStatus() != null;

        TaskDto task;
        if (hasDetails) {
            TaskDto current = findTaskByCode(code);
            TaskRequest update = new TaskRequest();
            update.setCode(code);
            update.setUsername(request.getUsername());
            update.setName(StringUtils.hasText(request.getName()) ? request.getName() : current.getName());
            update.setDescription(StringUtils.hasText(request.getDescription())
                    ? request.getDescription()
                    : current.getDescription());
            update.setSortOrder(request.getSortOrder() != null ? request.getSortOrder() : current.getSortOrder());
            if (request.getStatus() != null) {
                update.setStatus(request.getStatus());
            }
            task = authModuleClient.updateTask(code, update);
        } else {
            task = findTaskByCode(code);
        }

        return MessageResponseDTO.<TaskDto>builder()
                .success(true)
                .message("Task updated successfully")
                .data(task)
                .errors(null)
                .errorCode(0)
                .responseTime(LocalDateTime.now())
                .build();
    }

    @Override
    public MessageResponseDTO<TaskDto> getTask(Long id) {
        if (id == null) {
            return MessageResponseDTO.<TaskDto>builder()
                    .success(false)
                    .message("Invalid view request")
                    .data(null)
                    .errors(null)
                    .errorCode(400)
                    .responseTime(LocalDateTime.now())
                    .build();
        }
        TaskDto task = findTaskById(id);
        return MessageResponseDTO.<TaskDto>builder()
                .success(true)
                .message("Task found with ID: " + id)
                .data(task)
                .errors(null)
                .errorCode(0)
                .responseTime(LocalDateTime.now())
                .build();
    }

    @Override
    public MessageResponseDTO<TaskDto> updateTaskStatus(Long id, PageStatus status, String username) {
        if (id == null) {
            return MessageResponseDTO.<TaskDto>builder()
                    .success(false)
                    .message("Invalid status request")
                    .data(null)
                    .errors(null)
                    .errorCode(400)
                    .responseTime(LocalDateTime.now())
                    .build();
        }
        TaskDto current = findTaskById(id);
        TaskRequest update = new TaskRequest();
        update.setStatus(status);
        update.setUsername(username);
        TaskDto task = authModuleClient.updateTask(current.getCode(), update);
        return MessageResponseDTO.<TaskDto>builder()
                .success(true)
                .message("Task status updated successfully")
                .data(task)
                .errors(null)
                .errorCode(0)
                .responseTime(LocalDateTime.now())
                .build();
    }
    @Override
    public MessageResponseDTO<TaskDto> getTask(String code) {
        TaskDto task = authModuleClient.getTask(code);
        return MessageResponseDTO.<TaskDto>builder()
                .success(true)
                .message("Task details retrieved successfully")
                .data(task)
                .errors(null)
                .errorCode(0)
                .responseTime(LocalDateTime.now())
                .build();
    }

    @Override
    public MessageResponseDTO<List<TaskDto>> getAllTasks() {
        List<TaskDto> tasks = authModuleClient.getAllTasksCatalog();
        return MessageResponseDTO.<List<TaskDto>>builder()
                .success(true)
                .message("Tasks loaded successfully")
                .data(tasks)
                .errors(null)
                .errorCode(0)
                .responseTime(LocalDateTime.now())
                .build();
    }

    @Override
    public MessageResponseDTO<TaskReferenceDataDto> getReferenceData(TaskReferenceDataRequest request) {
        TaskPrivilegesDto privileges = resolvePrivileges(request);
        TaskReferenceDataDto data = TaskReferenceDataDto.builder()
                .defaultStatus(List.of(
                        ReferenceStatusDto.builder().code("ACTIVE").description("Active").build(),
                        ReferenceStatusDto.builder().code("INACTIVE").description("Inactive").build()
                ))
                .privileges(privileges)
                .build();

        return MessageResponseDTO.<TaskReferenceDataDto>builder()
                .success(true)
                .message("Reference data TASM retrieved successfully")
                .data(data)
                .errors(null)
                .errorCode(0)
                .responseTime(LocalDateTime.now())
                .build();
    }

    @Override
    public MessageResponseDTO<TaskFilterResultDto> filterList(TaskFilterRequest request) {
        List<TaskDto> tasks = authModuleClient.getAllTasksCatalogAll();

        TaskFilterSearch search = request != null ? request.getSearch() : null;
        String code = search != null ? normalize(search.getCode()) : null;
        String description = search != null ? normalize(search.getDescription()) : null;
        String status = search != null ? normalize(search.getStatus()) : null;

        List<TaskListItemDto> filtered = tasks.stream()
                .filter(task -> matches(code, task.getCode()))
                .filter(task -> matches(description, task.getDescription()))
                .filter(task -> matchesStatus(status, task.isActive()))
                .map(task -> TaskListItemDto.builder()
                        .id(task.getId())
                        .code(task.getCode())
                        .description(StringUtils.hasText(task.getDescription()) ? task.getDescription() : task.getName())
                        .status(task.isActive() ? "ACTIVE" : "INACTIVE")
                        .statusDescription(task.isActive() ? "Active" : "Inactive")
                        .createdDate(task.getCreatedDate())
                        .lastModifiedDate(task.getLastModifiedDate())
                        .createdBy(task.getCreatedBy())
                        .lastModifiedBy(task.getLastModifiedBy())
                        .build())
                .toList();

        Comparator<TaskListItemDto> comparator = resolveComparator(
                request != null ? request.getSortColumn() : null,
                request != null ? request.getSortDirection() : null
        );

        List<TaskListItemDto> sorted = filtered.stream().sorted(comparator).toList();

        int size = request != null && request.getSize() != null && request.getSize() > 0 ? request.getSize() : 10;
        int page = request != null && request.getPage() != null && request.getPage() >= 0 ? request.getPage() : 0;
        int totalElements = sorted.size();
        int totalPages = size == 0 ? 1 : (int) Math.ceil((double) totalElements / size);
        int fromIndex = Math.min(page * size, totalElements);
        int toIndex = Math.min(fromIndex + size, totalElements);
        List<TaskListItemDto> pageItems = sorted.subList(fromIndex, toIndex);

        TaskFilterResultDto result = TaskFilterResultDto.builder()
                .content(pageItems)
                .totalRecords(totalElements)
                .totalPages(totalPages)
                .page(page)
                .size(size)
                .build();

        return MessageResponseDTO.<TaskFilterResultDto>builder()
                .success(true)
                .message("Task list filtered successfully")
                .data(result)
                .errors(null)
                .errorCode(0)
                .responseTime(LocalDateTime.now())
                .build();
    }

    @Override
    public MessageResponseDTO<String> deactivateTask(String code) {
        authModuleClient.deactivateTask(code);
        return MessageResponseDTO.<String>builder()
                .success(true)
                .message("Task deactivated successfully")
                .data(code)
                .errors(null)
                .errorCode(0)
                .responseTime(LocalDateTime.now())
                .build();
    }

    private TaskDto findTaskById(Long id) {
        List<TaskDto> tasks = authModuleClient.getAllTasksCatalogAll();
        return tasks.stream()
                .filter(task -> task.getId() != null && task.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new com.zynolo_nexus.setting_service.exception.NotFoundException("task.notfound"));
    }

    private TaskDto findTaskByCode(String code) {
        if (!StringUtils.hasText(code)) {
            throw new com.zynolo_nexus.setting_service.exception.NotFoundException("task.notfound");
        }
        List<TaskDto> tasks = authModuleClient.getAllTasksCatalogAll();
        return tasks.stream()
                .filter(task -> code.equalsIgnoreCase(task.getCode()))
                .findFirst()
                .orElseThrow(() -> new com.zynolo_nexus.setting_service.exception.NotFoundException("task.notfound"));
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

    private Comparator<TaskListItemDto> resolveComparator(String sortColumn, String sortDirection) {
        String column = normalize(sortColumn);
        Comparator<TaskListItemDto> comparator;
        if ("description".equals(column)) {
            comparator = Comparator.comparing(TaskListItemDto::getDescription, String.CASE_INSENSITIVE_ORDER);
        } else if ("status".equals(column)) {
            comparator = Comparator.comparing(TaskListItemDto::getStatus, String.CASE_INSENSITIVE_ORDER);
        } else {
            comparator = Comparator.comparing(TaskListItemDto::getCode, String.CASE_INSENSITIVE_ORDER);
        }

        String dir = normalize(sortDirection);
        if ("desc".equals(dir)) {
            return comparator.reversed();
        }
        return comparator;
    }

    private TaskPrivilegesDto resolvePrivileges(TaskReferenceDataRequest request) {
        String username = request != null ? request.getUsername() : null;
        if (!StringUtils.hasText(username)) {
            username = getAuthenticatedUsername();
        }

        if (!StringUtils.hasText(username)) {
            return TaskPrivilegesDto.builder()
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
            return TaskPrivilegesDto.builder()
                    .add(false)
                    .update(false)
                    .view(false)
                    .search(false)
                    .delete(false)
                    .build();
        }

        var access = authModuleClient.getRolePageTaskAccess(roleCode);
        if (access == null || access.getPages() == null) {
            return TaskPrivilegesDto.builder()
                    .add(false)
                    .update(false)
                    .view(false)
                    .search(false)
                    .delete(false)
                    .build();
        }

        Map<String, Boolean> taskAccess = new HashMap<>();
        access.getPages().stream()
                .filter(page -> TASK_MANAGEMENT_CODE.equalsIgnoreCase(page.getPageCode()))
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

        return TaskPrivilegesDto.builder()
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
