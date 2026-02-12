package com.zynolo_nexus.setting_service.service.impl;

import com.zynolo_nexus.contracts.modules.ModuleDto;
import com.zynolo_nexus.contracts.modules.ModuleStatus;
import com.zynolo_nexus.contracts.pages.PageDto;
import com.zynolo_nexus.contracts.pages.RolePageTaskAccessDto;
import com.zynolo_nexus.contracts.pages.RolePageTaskAccessUpdateRequest;
import com.zynolo_nexus.contracts.pages.SectionDto;
import com.zynolo_nexus.contracts.pages.TaskDto;
import com.zynolo_nexus.setting_service.client.AuthModuleClient;
import com.zynolo_nexus.setting_service.dto.api.MessageResponseDTO;
import com.zynolo_nexus.setting_service.dto.request.RolePageTaskAccessCheckRequest;
import com.zynolo_nexus.setting_service.dto.request.RolePageTaskAccessPreviewRequest;
import com.zynolo_nexus.setting_service.dto.request.RolePageTaskAccessReferenceDataRequest;
import com.zynolo_nexus.setting_service.dto.request.RolePageTaskAccessUpdateByIdRequest;
import com.zynolo_nexus.setting_service.dto.request.RolePageTaskAccessViewRequest;
import com.zynolo_nexus.setting_service.dto.response.RolePageTaskAccessPrivilegesDto;
import com.zynolo_nexus.setting_service.dto.response.RolePageTaskAccessReferenceDataDto;
import com.zynolo_nexus.setting_service.dto.response.RolePageTaskAccessReferenceModuleTreeDto;
import com.zynolo_nexus.setting_service.dto.response.RolePageTaskAccessReferencePageTreeDto;
import com.zynolo_nexus.setting_service.dto.response.RolePageTaskAccessReferenceRoleDto;
import com.zynolo_nexus.setting_service.dto.response.RolePageTaskAccessReferenceSectionTreeDto;
import com.zynolo_nexus.setting_service.dto.response.RolePageTaskPrivilegeCheckDto;
import com.zynolo_nexus.setting_service.dto.response.RolePageTaskPrivilegePreviewDto;
import com.zynolo_nexus.setting_service.dto.response.RolePageTaskPrivilegePreviewTaskDto;
import com.zynolo_nexus.setting_service.exception.NotFoundException;
import com.zynolo_nexus.setting_service.model.Role;
import com.zynolo_nexus.setting_service.model.User;
import com.zynolo_nexus.setting_service.repository.RoleRepository;
import com.zynolo_nexus.setting_service.repository.UserRepository;
import com.zynolo_nexus.setting_service.service.RolePageTaskAccessService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class RolePageTaskAccessServiceImpl implements RolePageTaskAccessService {

    private static final String ROLE_PAGE_TASK_MANAGEMENT_CODE = "URTM";

    private final AuthModuleClient authModuleClient;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;

    @Override
    public MessageResponseDTO<RolePageTaskAccessDto> getRolePageTaskAccess(String roleCode) {
        RolePageTaskAccessDto dto = authModuleClient.getRolePageTaskAccess(roleCode);
        return MessageResponseDTO.<RolePageTaskAccessDto>builder()
                .success(true)
                .message("Role page tasks loaded successfully")
                .data(dto)
                .errors(null)
                .errorCode(0)
                .responseTime(LocalDateTime.now())
                .build();
    }

    @Override
    public MessageResponseDTO<RolePageTaskAccessDto> updateRolePageTaskAccess(RolePageTaskAccessUpdateRequest request) {
        RolePageTaskAccessDto dto = authModuleClient.updateRolePageTaskAccess(request);
        return MessageResponseDTO.<RolePageTaskAccessDto>builder()
                .success(true)
                .message("Role page tasks updated successfully")
                .data(dto)
                .errors(null)
                .errorCode(0)
                .responseTime(LocalDateTime.now())
                .build();
    }

    @Override
    public MessageResponseDTO<RolePageTaskAccessDto> getRolePageTaskAccess(RolePageTaskAccessViewRequest request) {
        String roleCode = resolveRoleCode(request != null ? request.getRoleId() : null,
                request != null ? request.getRoleCode() : null);
        RolePageTaskAccessDto dto = authModuleClient.getRolePageTaskAccess(roleCode);
        return MessageResponseDTO.<RolePageTaskAccessDto>builder()
                .success(true)
                .message("Role page tasks loaded successfully")
                .data(dto)
                .errors(null)
                .errorCode(0)
                .responseTime(LocalDateTime.now())
                .build();
    }

    @Override
    public MessageResponseDTO<RolePageTaskAccessDto> updateRolePageTaskAccess(RolePageTaskAccessUpdateByIdRequest request) {
        String roleCode = resolveRoleCode(request != null ? request.getRoleId() : null,
                request != null ? request.getRoleCode() : null);

        Map<Long, String> pageIdMap = loadPageCodeMap();
        Map<Long, String> taskIdMap = loadTaskCodeMap();

        List<RolePageTaskAccessUpdateRequest.PageTaskPermission> tasks = request != null && request.getTasks() != null
                ? request.getTasks().stream()
                .map(task -> {
                    String pageCode = resolvePageCode(task, pageIdMap);
                    String taskCode = resolveTaskCode(task, taskIdMap);
                    RolePageTaskAccessUpdateRequest.PageTaskPermission permission =
                            new RolePageTaskAccessUpdateRequest.PageTaskPermission();
                    permission.setPageCode(pageCode);
                    permission.setTaskCode(taskCode);
                    permission.setCanAccess(task != null && task.isCanAccess());
                    return permission;
                })
                .toList()
                : List.of();

        RolePageTaskAccessUpdateRequest payload = new RolePageTaskAccessUpdateRequest();
        payload.setRoleCode(roleCode);
        payload.setTasks(tasks);

        RolePageTaskAccessDto dto = authModuleClient.updateRolePageTaskAccess(payload);
        return MessageResponseDTO.<RolePageTaskAccessDto>builder()
                .success(true)
                .message("Role page tasks updated successfully")
                .data(dto)
                .errors(null)
                .errorCode(0)
                .responseTime(LocalDateTime.now())
                .build();
    }

    @Override
    public MessageResponseDTO<RolePageTaskPrivilegeCheckDto> checkRolePageTaskAccess(RolePageTaskAccessCheckRequest request) {
        String roleCode = resolveRoleCode(request != null ? request.getRoleId() : null,
                request != null ? request.getRoleCode() : null);

        Map<Long, String> pageIdMap = loadPageCodeMap();
        Map<Long, String> taskIdMap = loadTaskCodeMap();
        String pageCode = resolvePageCode(request, pageIdMap);
        String taskCode = resolveTaskCode(request, taskIdMap);

        RolePageTaskAccessDto dto = authModuleClient.getRolePageTaskAccess(roleCode);
        boolean canAccess = false;
        if (dto != null && dto.getPages() != null) {
            canAccess = dto.getPages().stream()
                    .filter(page -> page != null && StringUtils.hasText(page.getPageCode()))
                    .filter(page -> page.getPageCode().equalsIgnoreCase(pageCode))
                    .flatMap(page -> page.getTasks() != null ? page.getTasks().stream() : java.util.stream.Stream.empty())
                    .anyMatch(task -> task != null
                            && StringUtils.hasText(task.getTaskCode())
                            && task.getTaskCode().equalsIgnoreCase(taskCode)
                            && task.isCanAccess());
        }

        RolePageTaskPrivilegeCheckDto response = RolePageTaskPrivilegeCheckDto.builder()
                .roleId(request != null ? request.getRoleId() : null)
                .roleCode(roleCode)
                .pageId(request != null ? request.getPageId() : null)
                .pageCode(pageCode)
                .taskId(request != null ? request.getTaskId() : null)
                .taskCode(taskCode)
                .canAccess(canAccess)
                .build();

        return MessageResponseDTO.<RolePageTaskPrivilegeCheckDto>builder()
                .success(true)
                .message("Role page task privilege checked successfully")
                .data(response)
                .errors(null)
                .errorCode(0)
                .responseTime(LocalDateTime.now())
                .build();
    }

    @Override
    public MessageResponseDTO<RolePageTaskAccessReferenceDataDto> getReferenceData(RolePageTaskAccessReferenceDataRequest request) {
        RolePageTaskAccessPrivilegesDto privileges = resolvePrivileges(request);

        List<RolePageTaskAccessReferenceRoleDto> roles = roleRepository.findAll().stream()
                .filter(role -> role.getStatus() == null || role.getStatus().name().equalsIgnoreCase("ACTIVE"))
                .map(role -> RolePageTaskAccessReferenceRoleDto.builder()
                        .id(role.getId())
                        .code(role.getCode())
                        .description(role.getDescription())
                        .build())
                .toList();

        List<ModuleDto> modules = authModuleClient.getAllModulesAll().stream()
                .filter(module -> module != null && (module.getStatus() == null || module.getStatus() == ModuleStatus.ACTIVE))
                .toList();
        Map<String, ModuleDto> moduleByCode = new HashMap<>();
        for (ModuleDto module : modules) {
            if (module != null && StringUtils.hasText(module.getCode())) {
                moduleByCode.put(module.getCode().toLowerCase(), module);
            }
        }

        List<SectionDto> sections = authModuleClient.getAllSectionsAll().stream()
                .filter(section -> section != null && section.isActive())
                .toList();
        Map<String, List<SectionDto>> sectionsByModule = new HashMap<>();
        for (SectionDto section : sections) {
            if (section != null && StringUtils.hasText(section.getModuleCode())) {
                String moduleKey = section.getModuleCode().toLowerCase();
                if (moduleByCode.containsKey(moduleKey)) {
                    sectionsByModule.computeIfAbsent(moduleKey, key -> new java.util.ArrayList<>()).add(section);
                }
            }
        }

        List<PageDto> pages = authModuleClient.getAllPagesAll().stream()
                .filter(page -> page != null && page.isActive())
                .toList();
        Map<String, List<PageDto>> pagesBySection = new HashMap<>();
        for (PageDto page : pages) {
            if (page != null && StringUtils.hasText(page.getSectionCode())) {
                String sectionKey = page.getSectionCode().toLowerCase();
                pagesBySection.computeIfAbsent(sectionKey, key -> new java.util.ArrayList<>()).add(page);
            }
        }

        List<RolePageTaskAccessReferenceModuleTreeDto> moduleTrees = modules.stream()
                .map(module -> {
                    String moduleKey = module.getCode() != null ? module.getCode().toLowerCase() : null;
                    List<SectionDto> moduleSections = moduleKey != null
                            ? sectionsByModule.getOrDefault(moduleKey, List.of())
                            : List.of();
                    List<RolePageTaskAccessReferenceSectionTreeDto> sectionTrees = moduleSections.stream()
                            .map(section -> {
                                String sectionKey = section.getCode() != null ? section.getCode().toLowerCase() : null;
                                List<PageDto> sectionPages = sectionKey != null
                                        ? pagesBySection.getOrDefault(sectionKey, List.of())
                                        : List.of();
                                List<RolePageTaskAccessReferencePageTreeDto> pageTrees = sectionPages.stream()
                                        .map(page -> RolePageTaskAccessReferencePageTreeDto.builder()
                                                .code(page.getCode())
                                                .description(StringUtils.hasText(page.getDescription())
                                                        ? page.getDescription()
                                                        : page.getName())
                                                .build())
                                        .toList();
                                return RolePageTaskAccessReferenceSectionTreeDto.builder()
                                        .code(section.getCode())
                                        .description(StringUtils.hasText(section.getDescription())
                                                ? section.getDescription()
                                                : section.getName())
                                        .pages(pageTrees)
                                        .build();
                            })
                            .toList();
                    return RolePageTaskAccessReferenceModuleTreeDto.builder()
                            .code(module.getCode())
                            .description(StringUtils.hasText(module.getDescription())
                                    ? module.getDescription()
                                    : module.getName())
                            .sections(sectionTrees)
                            .build();
                })
                .toList();

        RolePageTaskAccessReferenceDataDto data = RolePageTaskAccessReferenceDataDto.builder()
                .userRole(roles)
                .modules(moduleTrees)
                .privileges(privileges)
                .build();

        return MessageResponseDTO.<RolePageTaskAccessReferenceDataDto>builder()
                .success(true)
                .message("Reference data URTM retrieved successfully")
                .data(data)
                .errors(null)
                .errorCode(0)
                .responseTime(LocalDateTime.now())
                .build();
    }

    @Override
    public MessageResponseDTO<RolePageTaskPrivilegePreviewDto> previewPageTasks(RolePageTaskAccessPreviewRequest request) {
        String roleCode = resolveRoleCode(request != null ? request.getRoleId() : null,
                request != null ? request.getRoleCode() : null);

        Map<Long, String> pageIdMap = loadPageCodeMap();
        Map<String, Long> taskIdMap = loadTaskIdMap();
        String pageCode = resolvePageCode(request, pageIdMap);

        RolePageTaskAccessDto dto = authModuleClient.getRolePageTaskAccess(roleCode);
        List<RolePageTaskPrivilegePreviewTaskDto> tasks = List.of();
        if (dto != null && dto.getPages() != null) {
            tasks = dto.getPages().stream()
                    .filter(page -> page != null && StringUtils.hasText(page.getPageCode()))
                    .filter(page -> page.getPageCode().equalsIgnoreCase(pageCode))
                    .findFirst()
                    .map(page -> page.getTasks() != null ? page.getTasks().stream()
                            .map(task -> RolePageTaskPrivilegePreviewTaskDto.builder()
                                    .taskId(task != null && StringUtils.hasText(task.getTaskCode())
                                            ? taskIdMap.get(task.getTaskCode().toLowerCase())
                                            : null)
                                    .taskCode(task != null ? task.getTaskCode() : null)
                                    .taskName(task != null ? task.getTaskName() : null)
                                    .canAccess(task != null && task.isCanAccess())
                                    .build())
                            .toList() : List.<RolePageTaskPrivilegePreviewTaskDto>of())
                    .orElse(List.of());
        }

        if (request != null && StringUtils.hasText(request.getTaskCode())) {
            String filterCode = request.getTaskCode().trim().toLowerCase();
            tasks = tasks.stream()
                    .filter(task -> task != null && StringUtils.hasText(task.getTaskCode())
                            && task.getTaskCode().toLowerCase().equals(filterCode))
                    .toList();
        }

        RolePageTaskPrivilegePreviewDto response = RolePageTaskPrivilegePreviewDto.builder()
                .roleId(request != null ? request.getRoleId() : null)
                .roleCode(roleCode)
                .pageId(request != null ? request.getPageId() : null)
                .pageCode(pageCode)
                .tasks(tasks)
                .build();

        return MessageResponseDTO.<RolePageTaskPrivilegePreviewDto>builder()
                .success(true)
                .message("Role page task privileges loaded successfully")
                .data(response)
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

    private Map<Long, String> loadPageCodeMap() {
        List<PageDto> pages = authModuleClient.getAllPagesAll();
        Map<Long, String> map = new HashMap<>();
        if (pages != null) {
            for (PageDto page : pages) {
                if (page != null && page.getId() != null && StringUtils.hasText(page.getCode())) {
                    map.put(page.getId(), page.getCode());
                }
            }
        }
        return map;
    }

    private Map<Long, String> loadTaskCodeMap() {
        List<TaskDto> tasks = authModuleClient.getAllTasksCatalogAll();
        Map<Long, String> map = new HashMap<>();
        if (tasks != null) {
            for (TaskDto task : tasks) {
                if (task != null && task.getId() != null && StringUtils.hasText(task.getCode())) {
                    map.put(task.getId(), task.getCode());
                }
            }
        }
        return map;
    }

    private Map<String, Long> loadTaskIdMap() {
        List<TaskDto> tasks = authModuleClient.getAllTasksCatalogAll();
        Map<String, Long> map = new HashMap<>();
        if (tasks != null) {
            for (TaskDto task : tasks) {
                if (task != null && task.getId() != null && StringUtils.hasText(task.getCode())) {
                    map.put(task.getCode().toLowerCase(), task.getId());
                }
            }
        }
        return map;
    }

    private String resolvePageCode(RolePageTaskAccessUpdateByIdRequest.PageTaskPermission task,
                                   Map<Long, String> pageIdMap) {
        if (task != null) {
            if (task.getPageId() != null) {
                String code = pageIdMap.get(task.getPageId());
                if (!StringUtils.hasText(code)) {
                    throw new NotFoundException("page.notfound");
                }
                return code;
            }
            if (StringUtils.hasText(task.getPageCode())) {
                return task.getPageCode();
            }
        }
        throw new NotFoundException("page.notfound");
    }

    private String resolveTaskCode(RolePageTaskAccessUpdateByIdRequest.PageTaskPermission task,
                                   Map<Long, String> taskIdMap) {
        if (task != null) {
            if (task.getTaskId() != null) {
                String code = taskIdMap.get(task.getTaskId());
                if (!StringUtils.hasText(code)) {
                    throw new NotFoundException("task.notfound");
                }
                return code;
            }
            if (StringUtils.hasText(task.getTaskCode())) {
                return task.getTaskCode();
            }
        }
        throw new NotFoundException("task.notfound");
    }

    private String resolvePageCode(RolePageTaskAccessCheckRequest request, Map<Long, String> pageIdMap) {
        if (request != null && request.getPageId() != null) {
            String code = pageIdMap.get(request.getPageId());
            if (!StringUtils.hasText(code)) {
                throw new NotFoundException("page.notfound");
            }
            return code;
        }
        if (request != null && StringUtils.hasText(request.getPageCode())) {
            return request.getPageCode();
        }
        throw new NotFoundException("page.notfound");
    }

    private String resolvePageCode(RolePageTaskAccessPreviewRequest request, Map<Long, String> pageIdMap) {
        if (request != null && request.getPageId() != null) {
            String code = pageIdMap.get(request.getPageId());
            if (!StringUtils.hasText(code)) {
                throw new NotFoundException("page.notfound");
            }
            return code;
        }
        if (request != null && StringUtils.hasText(request.getPageCode())) {
            return request.getPageCode();
        }
        throw new NotFoundException("page.notfound");
    }

    private String resolveTaskCode(RolePageTaskAccessCheckRequest request, Map<Long, String> taskIdMap) {
        if (request != null && request.getTaskId() != null) {
            String code = taskIdMap.get(request.getTaskId());
            if (!StringUtils.hasText(code)) {
                throw new NotFoundException("task.notfound");
            }
            return code;
        }
        if (request != null && StringUtils.hasText(request.getTaskCode())) {
            return request.getTaskCode();
        }
        throw new NotFoundException("task.notfound");
    }

    private RolePageTaskAccessPrivilegesDto resolvePrivileges(RolePageTaskAccessReferenceDataRequest request) {
        String username = request != null ? request.getUsername() : null;
        if (!StringUtils.hasText(username)) {
            username = getAuthenticatedUsername();
        }

        if (!StringUtils.hasText(username)) {
            return RolePageTaskAccessPrivilegesDto.builder()
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
            return RolePageTaskAccessPrivilegesDto.builder()
                    .add(false)
                    .update(false)
                    .view(false)
                    .search(false)
                    .delete(false)
                    .build();
        }

        RolePageTaskAccessDto access = authModuleClient.getRolePageTaskAccess(roleCode);
        if (access == null || access.getPages() == null) {
            return RolePageTaskAccessPrivilegesDto.builder()
                    .add(false)
                    .update(false)
                    .view(false)
                    .search(false)
                    .delete(false)
                    .build();
        }

        Map<String, Boolean> taskAccess = new HashMap<>();
        access.getPages().stream()
                .filter(page -> ROLE_PAGE_TASK_MANAGEMENT_CODE.equalsIgnoreCase(page.getPageCode()))
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

        return RolePageTaskAccessPrivilegesDto.builder()
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
        return value.replaceAll("[^A-Za-z0-9]", "").toUpperCase();
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
