package com.zynolo_nexus.setting_service.service.impl;

import com.zynolo_nexus.contracts.pages.PageDto;
import com.zynolo_nexus.contracts.pages.RolePageTaskAccessDto;
import com.zynolo_nexus.contracts.pages.RolePageTaskAccessUpdateRequest;
import com.zynolo_nexus.contracts.pages.TaskDto;
import com.zynolo_nexus.setting_service.client.AuthModuleClient;
import com.zynolo_nexus.setting_service.dto.api.MessageResponseDTO;
import com.zynolo_nexus.setting_service.dto.request.RolePageTaskAccessCheckRequest;
import com.zynolo_nexus.setting_service.dto.request.RolePageTaskAccessUpdateByIdRequest;
import com.zynolo_nexus.setting_service.dto.request.RolePageTaskAccessViewRequest;
import com.zynolo_nexus.setting_service.dto.response.RolePageTaskPrivilegeCheckDto;
import com.zynolo_nexus.setting_service.exception.NotFoundException;
import com.zynolo_nexus.setting_service.model.Role;
import com.zynolo_nexus.setting_service.repository.RoleRepository;
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

    private final AuthModuleClient authModuleClient;
    private final RoleRepository roleRepository;

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
}
