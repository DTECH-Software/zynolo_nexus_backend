package com.zynolo_nexus.auth_service.service.impl;

import com.zynolo_nexus.auth_service.context.CompanyContext;
import com.zynolo_nexus.auth_service.exception.BadRequestException;
import com.zynolo_nexus.auth_service.exception.NotFoundException;
import com.zynolo_nexus.auth_service.enums.ModuleStatus;
import com.zynolo_nexus.auth_service.model.Module;
import com.zynolo_nexus.auth_service.model.Page;
import com.zynolo_nexus.auth_service.model.PageTask;
import com.zynolo_nexus.auth_service.model.Role;
import com.zynolo_nexus.auth_service.model.RolePageTaskAccess;
import com.zynolo_nexus.auth_service.model.Section;
import com.zynolo_nexus.auth_service.model.Task;
import com.zynolo_nexus.auth_service.repository.ModuleRepository;
import com.zynolo_nexus.auth_service.repository.PageRepository;
import com.zynolo_nexus.auth_service.repository.PageTaskRepository;
import com.zynolo_nexus.auth_service.repository.RolePageTaskAccessRepository;
import com.zynolo_nexus.auth_service.repository.RoleRepository;
import com.zynolo_nexus.auth_service.repository.SectionRepository;
import com.zynolo_nexus.auth_service.repository.TaskRepository;
import com.zynolo_nexus.auth_service.service.RolePageTaskAccessManagementService;
import com.zynolo_nexus.contracts.pages.RolePageTaskAccessDto;
import com.zynolo_nexus.contracts.pages.RolePageTaskAccessUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class RolePageTaskAccessManagementServiceImpl implements RolePageTaskAccessManagementService {

    private final RoleRepository roleRepository;
    private final ModuleRepository moduleRepository;
    private final SectionRepository sectionRepository;
    private final PageRepository pageRepository;
    private final PageTaskRepository pageTaskRepository;
    private final TaskRepository taskRepository;
    private final RolePageTaskAccessRepository rolePageTaskAccessRepository;

    @Value("${app.default.company-id:1}")
    private Long defaultCompanyId;

    @Override
    public RolePageTaskAccessDto getRolePageTaskAccess(String roleCode) {
        Role role = resolveRole(roleCode);
        Long companyId = resolveCompanyId();

        List<Module> modules = moduleRepository.findAllActiveOrderBySortOrderAsc(ModuleStatus.ACTIVE);
        List<Section> sections = sectionRepository.findAllByActiveTrueOrderBySortOrderAsc();
        List<Page> pages = pageRepository.findAllByActiveTrueOrderBySortOrderAsc();
        List<PageTask> tasks = pageTaskRepository.findAllByOrderByIdAsc();

        Map<Long, List<Section>> sectionsByModule = new HashMap<>();
        for (Section section : sections) {
            sectionsByModule.computeIfAbsent(section.getModule().getId(), k -> new ArrayList<>()).add(section);
        }

        Map<Long, List<Page>> pagesBySection = new HashMap<>();
        for (Page page : pages) {
            pagesBySection.computeIfAbsent(page.getSection().getId(), k -> new ArrayList<>()).add(page);
        }

        Map<Long, List<PageTask>> tasksByPage = new HashMap<>();
        for (PageTask task : tasks) {
            tasksByPage.computeIfAbsent(task.getPage().getId(), k -> new ArrayList<>()).add(task);
        }

        Map<Long, Boolean> accessMap = rolePageTaskAccessRepository.findByRoleAndCompanyId(role, companyId).stream()
                .collect(Collectors.toMap(
                        a -> a.getPageTask().getId(),
                        a -> Boolean.TRUE.equals(a.getCanAccess()),
                        (left, right) -> left
                ));

        List<RolePageTaskAccessDto.PagePermissionItem> pageItems = new ArrayList<>();
        for (Module module : modules) {
            List<Section> moduleSections = sectionsByModule.getOrDefault(module.getId(), List.of());
            for (Section section : moduleSections) {
                List<Page> sectionPages = pagesBySection.getOrDefault(section.getId(), List.of());
                for (Page page : sectionPages) {
                    List<PageTask> pageTasks = tasksByPage.getOrDefault(page.getId(), List.of());
                    List<RolePageTaskAccessDto.TaskPermissionItem> taskItems = pageTasks.stream()
                            .map(task -> RolePageTaskAccessDto.TaskPermissionItem.builder()
                                    .taskCode(task.getTask().getCode())
                                    .taskName(task.getTask().getName())
                                    .canAccess(accessMap.getOrDefault(task.getId(), Boolean.FALSE))
                                    .build())
                            .toList();

                    pageItems.add(RolePageTaskAccessDto.PagePermissionItem.builder()
                            .moduleCode(module.getCode())
                            .moduleName(module.getName())
                            .sectionCode(section.getCode())
                            .sectionName(section.getName())
                            .pageCode(page.getCode())
                            .pageName(page.getName())
                            .tasks(taskItems)
                            .build());
                }
            }
        }

        return RolePageTaskAccessDto.builder()
                .roleCode(roleCode)
                .pages(pageItems)
                .build();
    }

    @Override
    public RolePageTaskAccessDto updateRolePageTaskAccess(RolePageTaskAccessUpdateRequest request) {
        if (request == null || request.getRoleCode() == null) {
            throw new BadRequestException("role.page.task.invalid");
        }

        Role role = resolveRole(request.getRoleCode());
        Long companyId = resolveCompanyId();

        List<RolePageTaskAccess> existing = rolePageTaskAccessRepository.findByRoleAndCompanyId(role, companyId);
        rolePageTaskAccessRepository.deleteAll(existing);

        if (request.getTasks() != null) {
            for (RolePageTaskAccessUpdateRequest.PageTaskPermission perm : request.getTasks()) {
                if (!perm.isCanAccess()) {
                    continue;
                }

                Page page = pageRepository.findByCode(perm.getPageCode())
                        .orElseThrow(() -> new NotFoundException("page.notfound"));

                Task taskEntity = taskRepository.findByCode(perm.getTaskCode())
                        .orElseThrow(() -> new NotFoundException("task.notfound"));

                PageTask task = pageTaskRepository.findByPageAndTask(page, taskEntity)
                        .orElseThrow(() -> new NotFoundException("page.task.notfound"));

                RolePageTaskAccess access = RolePageTaskAccess.builder()
                        .role(role)
                        .pageTask(task)
                        .companyId(companyId)
                        .canAccess(true)
                        .build();
                rolePageTaskAccessRepository.save(access);
            }
        }

        return getRolePageTaskAccess(request.getRoleCode());
    }

    private Role resolveRole(String roleCode) {
        if (!org.springframework.util.StringUtils.hasText(roleCode)) {
            throw new BadRequestException("role.page.task.invalid");
        }
        return roleRepository.findByCodeIgnoreCase(roleCode)
                .orElseThrow(() -> new NotFoundException("role.page.task.notfound"));
    }

    private Long resolveCompanyId() {
        Long companyId = CompanyContext.getCompanyId();
        return companyId != null ? companyId : defaultCompanyId;
    }
}
