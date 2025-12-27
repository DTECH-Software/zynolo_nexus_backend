package com.zynolo_nexus.auth_service.controller.internal;

import com.zynolo_nexus.auth_service.service.ModuleManagementService;
import com.zynolo_nexus.contracts.modules.ModuleDto;
import com.zynolo_nexus.contracts.modules.ModuleRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
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
        return moduleManagementService.createModule(request);
    }

    @PutMapping("/{code}")
    public ModuleDto updateModule(@PathVariable String code, @RequestBody ModuleRequest request) {
        return moduleManagementService.updateModule(code, request);
    }

    @GetMapping
    public List<ModuleDto> getAllModules() {
        return moduleManagementService.getAllModules();
    }

    @DeleteMapping("/{code}")
    public void deactivateModule(@PathVariable String code) {
        moduleManagementService.deactivateModule(code);
    }
}
