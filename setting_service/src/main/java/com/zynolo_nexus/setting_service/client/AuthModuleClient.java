package com.zynolo_nexus.setting_service.client;

import com.zynolo_nexus.contracts.modules.ModuleDto;
import com.zynolo_nexus.contracts.modules.ModuleRequest;
import com.zynolo_nexus.contracts.modules.RoleModuleAccessDto;
import com.zynolo_nexus.contracts.modules.RoleModuleAccessUpdateRequest;
import com.zynolo_nexus.contracts.pages.PageDto;
import com.zynolo_nexus.contracts.pages.PageRequest;
import com.zynolo_nexus.contracts.pages.PageTaskDto;
import com.zynolo_nexus.contracts.pages.PageTaskRequest;
import com.zynolo_nexus.contracts.pages.RolePageTaskAccessDto;
import com.zynolo_nexus.contracts.pages.RolePageTaskAccessUpdateRequest;
import com.zynolo_nexus.contracts.pages.SectionDto;
import com.zynolo_nexus.contracts.pages.SectionRequest;
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

    @PutMapping("/internal/modules/{code}")
    ModuleDto updateModule(@PathVariable("code") String code,
                           @RequestBody ModuleRequest request);

    @GetMapping("/internal/modules")
    List<ModuleDto> getAllModules();

    @DeleteMapping("/internal/modules/{code}")
    void deactivateModule(@PathVariable("code") String code);

    @GetMapping("/internal/role-modules/{roleCode}")
    RoleModuleAccessDto getRoleModuleAccess(@PathVariable("roleCode") String roleCode);

    @PutMapping("/internal/role-modules")
    RoleModuleAccessDto updateRoleModuleAccess(@RequestBody RoleModuleAccessUpdateRequest request);

    @PostMapping("/internal/sections")
    SectionDto createSection(@RequestBody SectionRequest request);

    @PutMapping("/internal/sections/{code}")
    SectionDto updateSection(@PathVariable("code") String code, @RequestBody SectionRequest request);

    @GetMapping("/internal/sections")
    List<SectionDto> getAllSections();

    @DeleteMapping("/internal/sections/{code}")
    void deactivateSection(@PathVariable("code") String code);

    @PostMapping("/internal/pages")
    PageDto createPage(@RequestBody PageRequest request);

    @PutMapping("/internal/pages/{code}")
    PageDto updatePage(@PathVariable("code") String code, @RequestBody PageRequest request);

    @GetMapping("/internal/pages")
    List<PageDto> getAllPages();

    @DeleteMapping("/internal/pages/{code}")
    void deactivatePage(@PathVariable("code") String code);

    @PostMapping("/internal/page-tasks")
    PageTaskDto createTask(@RequestBody PageTaskRequest request);

    @PutMapping("/internal/page-tasks/{pageCode}/{taskCode}")
    PageTaskDto updateTask(@PathVariable("pageCode") String pageCode,
                           @PathVariable("taskCode") String taskCode,
                           @RequestBody PageTaskRequest request);

    @GetMapping("/internal/page-tasks")
    List<PageTaskDto> getAllTasks();

    @DeleteMapping("/internal/page-tasks/{pageCode}/{taskCode}")
    void deactivateTask(@PathVariable("pageCode") String pageCode,
                        @PathVariable("taskCode") String taskCode);

    @GetMapping("/internal/role-page-tasks/{roleCode}")
    RolePageTaskAccessDto getRolePageTaskAccess(@PathVariable("roleCode") String roleCode);

    @PutMapping("/internal/role-page-tasks")
    RolePageTaskAccessDto updateRolePageTaskAccess(@RequestBody RolePageTaskAccessUpdateRequest request);
}
