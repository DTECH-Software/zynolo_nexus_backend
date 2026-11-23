package com.zynolo_nexus.setting_service.client;

import com.zynolo_nexus.contracts.modules.ModuleDto;
import com.zynolo_nexus.contracts.modules.ModuleRequest;
import com.zynolo_nexus.contracts.modules.RoleModuleAccessDto;
import com.zynolo_nexus.contracts.modules.RoleModuleAccessUpdateRequest;
import com.zynolo_nexus.contracts.pages.PageDto;
import com.zynolo_nexus.contracts.pages.PageRequest;
import com.zynolo_nexus.contracts.pages.PageStatusRequest;
import com.zynolo_nexus.contracts.pages.PageTaskDto;
import com.zynolo_nexus.contracts.pages.PageTaskRequest;
import com.zynolo_nexus.contracts.pages.RolePageTaskAccessDto;
import com.zynolo_nexus.contracts.pages.RolePageTaskAccessUpdateRequest;
import com.zynolo_nexus.contracts.pages.SectionDto;
import com.zynolo_nexus.contracts.pages.SectionRequest;
import com.zynolo_nexus.contracts.pages.SectionStatusRequest;
import com.zynolo_nexus.contracts.pages.TaskDto;
import com.zynolo_nexus.contracts.pages.TaskRequest;
import org.springframework.cloud.openfeign.FeignClient;
import com.zynolo_nexus.setting_service.config.FeignInternalAuthConfig;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(
        name = "auth-service",
        url = "${auth.service.url}",
        configuration = FeignInternalAuthConfig.class
)
public interface AuthModuleClient {

    @PostMapping("/internal/modules")
    ModuleDto createModule(@RequestBody ModuleRequest request);

    @PostMapping("/internal/modules/{code}/update")
    ModuleDto updateModule(@PathVariable("code") String code,
                           @RequestBody ModuleRequest request);

    @PostMapping("/internal/modules/list")
    List<ModuleDto> getAllModules();

    @PostMapping("/internal/modules/list-all")
    List<ModuleDto> getAllModulesAll();

    @PostMapping("/internal/modules/{code}/deactivate")
    void deactivateModule(@PathVariable("code") String code);

    @PostMapping("/internal/role-modules/{roleCode}/get")
    RoleModuleAccessDto getRoleModuleAccess(@PathVariable("roleCode") String roleCode);

    @PostMapping("/internal/role-modules/update")
    RoleModuleAccessDto updateRoleModuleAccess(@RequestBody RoleModuleAccessUpdateRequest request);

    @PostMapping("/internal/sections")
    SectionDto createSection(@RequestBody SectionRequest request);

    @PostMapping("/internal/sections/{code}/update")
    SectionDto updateSection(@PathVariable("code") String code, @RequestBody SectionRequest request);

    @PostMapping("/internal/sections/list")
    List<SectionDto> getAllSections();

    @PostMapping("/internal/sections/list-all")
    List<SectionDto> getAllSectionsAll();

    @PostMapping("/internal/sections/{code}/status")
    SectionDto updateSectionStatus(@PathVariable("code") String code, @RequestBody SectionStatusRequest request);

    @PostMapping("/internal/sections/{code}/deactivate")
    void deactivateSection(@PathVariable("code") String code);

    @PostMapping("/internal/pages")
    PageDto createPage(@RequestBody PageRequest request);

    @PostMapping("/internal/pages/{code}/update")
    PageDto updatePage(@PathVariable("code") String code, @RequestBody PageRequest request);

    @PostMapping("/internal/pages/{code}/get")
    PageDto getPage(@PathVariable("code") String code);

    @PostMapping("/internal/pages/list")
    List<PageDto> getAllPages();

    @PostMapping("/internal/pages/list-all")
    List<PageDto> getAllPagesAll();

    @PostMapping("/internal/pages/{code}/status")
    PageDto updatePageStatus(@PathVariable("code") String code, @RequestBody PageStatusRequest request);

    @PostMapping("/internal/pages/{code}/deactivate")
    void deactivatePage(@PathVariable("code") String code);

    @PostMapping("/internal/tasks")
    TaskDto createTask(@RequestBody TaskRequest request);

    @PostMapping("/internal/tasks/{code}/update")
    TaskDto updateTask(@PathVariable("code") String code, @RequestBody TaskRequest request);

    @PostMapping("/internal/tasks/{code}/get")
    TaskDto getTask(@PathVariable("code") String code);

    @PostMapping("/internal/tasks/list")
    List<TaskDto> getAllTasksCatalog();

    @PostMapping("/internal/tasks/list-all")
    List<TaskDto> getAllTasksCatalogAll();

    @PostMapping("/internal/tasks/{code}/deactivate")
    void deactivateTask(@PathVariable("code") String code);

    @PostMapping("/internal/page-tasks")
    PageTaskDto createTask(@RequestBody PageTaskRequest request);

    @PostMapping("/internal/page-tasks/{pageCode}/{taskCode}/update")
    PageTaskDto updateTask(@PathVariable("pageCode") String pageCode,
                           @PathVariable("taskCode") String taskCode,
                           @RequestBody PageTaskRequest request);

    @PostMapping("/internal/page-tasks/list")
    List<PageTaskDto> getAllTasks();

    @PostMapping("/internal/page-tasks/{pageCode}/{taskCode}/deactivate")
    void deactivateTask(@PathVariable("pageCode") String pageCode,
                        @PathVariable("taskCode") String taskCode);

    @PostMapping("/internal/role-page-tasks/{roleCode}/get")
    RolePageTaskAccessDto getRolePageTaskAccess(@PathVariable("roleCode") String roleCode);

    @PostMapping("/internal/role-page-tasks/update")
    RolePageTaskAccessDto updateRolePageTaskAccess(@RequestBody RolePageTaskAccessUpdateRequest request);
}
