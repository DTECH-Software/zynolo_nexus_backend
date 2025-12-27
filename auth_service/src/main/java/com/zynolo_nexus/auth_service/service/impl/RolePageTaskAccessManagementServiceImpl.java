package com.zynolo_nexus.auth_service.service.impl;

import com.zynolo_nexus.auth_service.enums.RoleCode;
import com.zynolo_nexus.auth_service.exception.BadRequestException;
import com.zynolo_nexus.auth_service.exception.NotFoundException;
import com.zynolo_nexus.auth_service.model.Module;
import com.zynolo_nexus.auth_service.model.Page;
import com.zynolo_nexus.auth_service.model.PageTask;
import com.zynolo_nexus.auth_service.model.Role;
import com.zynolo_nexus.auth_service.model.RolePageTaskAccess;
import com.zynolo_nexus.auth_service.model.Section;
import com.zynolo_nexus.auth_service.repository.ModuleRepository;
import com.zynolo_nexus.auth_service.repository.PageRepository;
import com.zynolo_nexus.auth_service.repository.PageTaskRepository;
import com.zynolo_nexus.auth_service.repository.RolePageTaskAccessRepository;
import com.zynolo_nexus.auth_service.repository.RoleRepository;
import com.zynolo_nexus.auth_service.repository.SectionRepository;
import com.zynolo_nexus.auth_service.service.RolePageTaskAccessManagementService;
import com.zynolo_nexus.contracts.pages.RolePageTaskAccessDto;
import com.zynolo_nexus.contracts.pages.RolePageTaskAccessUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RolePageTaskAccessManagementServiceImpl implements RolePageTaskAccessManagementService {

    private final RoleRepository roleRepository;
    private final ModuleRepository moduleRepository;
    private final SectionRepository sectionRepository;
    private final PageRepository pageRepository;
    private final PageTaskRepository pageTaskRepository;
    private final RolePageTaskAccessRepository rolePageTaskAccessRepository;

    @Override
    public RolePageTaskAccessDto getRolePageTaskAccess(String roleCode) {
        Role role = resolveRole(roleCode);

        List<Module> modules = moduleRepository.findAllByActiveTrueOrderBySortOrderAsc();
        List<Section> sections = sectionRepository.findAllByActiveTrueOrderBySortOrderAsc();
        List<Page> pages = pageRepository.findAllByActiveTrueOrderBySortOrderAsc();
        List<PageTask> tasks = pageTaskRepository.findAllByActiveTrueOrderBySortOrderAsc();

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

        Map<Long, Boolean> accessMap = rolePageTaskAccessRepository.findByRole(role).stream()
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
                                    .taskCode(task.getCode())
                                    .taskName(task.getName())
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

        List<RolePageTaskAccess> existing = rolePageTaskAccessRepository.findByRole(role);
        rolePageTaskAccessRepository.deleteAll(existing);

        if (request.getTasks() != null) {
            for (RolePageTaskAccessUpdateRequest.PageTaskPermission perm : request.getTasks()) {
                if (!perm.isCanAccess()) {
                    continue;
                }

                Page page = pageRepository.findByCode(perm.getPageCode())
                        .orElseThrow(() -> new NotFoundException("page.notfound"));

                PageTask task = pageTaskRepository.findByPageAndCode(page, perm.getTaskCode())
                        .orElseThrow(() -> new NotFoundException("page.task.notfound"));

                RolePageTaskAccess access = RolePageTaskAccess.builder()
                        .role(role)
                        .pageTask(task)
                        .canAccess(true)
                        .build();
                rolePageTaskAccessRepository.save(access);
            }
        }

        return getRolePageTaskAccess(request.getRoleCode());
    }

    private Role resolveRole(String roleCode) {
        try {
            RoleCode rc = RoleCode.valueOf(roleCode);
            return roleRepository.findByCode(rc)
                    .orElseThrow(() -> new NotFoundException("role.page.task.notfound"));
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException("role.page.task.invalid");
        }
    }
}
