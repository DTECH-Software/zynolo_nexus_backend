package com.zynolo_nexus.auth_service.controller.internal;

import com.zynolo_nexus.auth_service.config.AuditUserContext;
import com.zynolo_nexus.auth_service.service.ModuleManagementService;
import com.zynolo_nexus.contracts.modules.ModuleDto;
import com.zynolo_nexus.contracts.modules.ModuleRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/internal/modules")
@RequiredArgsConstructor
public class ModuleInternalController {

    private final ModuleManagementService moduleManagementService;

    @PostMapping
    public ModuleDto createModule(@RequestBody ModuleRequest request) {
        return AuditUserContext.runWith(request != null ? request.getUsername() : null,
                () -> moduleManagementService.createModule(request));
    }

    @PostMapping("/{code}/update")
    public ModuleDto updateModule(@PathVariable String code, @RequestBody ModuleRequest request) {
        return AuditUserContext.runWith(request != null ? request.getUsername() : null,
                () -> moduleManagementService.updateModule(code, request));
    }

    @PostMapping("/list")
    public List<ModuleDto> getAllModules() {
        return moduleManagementService.getAllModules();
    }

    @PostMapping("/list-all")
    public List<ModuleDto> getAllModulesAll() {
        return moduleManagementService.getAllModulesAll();
    }

    @PostMapping("/{code}/deactivate")
    public void deactivateModule(@PathVariable String code) {
        moduleManagementService.deactivateModule(code);
    }
}
