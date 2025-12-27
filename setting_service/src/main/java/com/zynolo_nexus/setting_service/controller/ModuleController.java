package com.zynolo_nexus.setting_service.controller;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.zynolo_nexus.contracts.modules.ModuleDto;
import com.zynolo_nexus.contracts.modules.ModuleRequest;
import com.zynolo_nexus.setting_service.dto.api.MessageResponseDTO;
import com.zynolo_nexus.setting_service.service.ModuleService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/setting/modules")
@RequiredArgsConstructor
public class ModuleController {

    private final ModuleService moduleService;

    @PostMapping
    public MessageResponseDTO<ModuleDto> createModule(@RequestBody ModuleRequest request) {
        return moduleService.createModule(request);
    }

    @PutMapping("/{code}")
    public MessageResponseDTO<ModuleDto> updateModule(
            @PathVariable String code,
            @RequestBody ModuleRequest request) {
        return moduleService.updateModule(code, request);
    }

    @GetMapping
    public MessageResponseDTO<List<ModuleDto>> getAllModules() {
        return moduleService.getAllModules();
    }

    @DeleteMapping("/{code}")
    public MessageResponseDTO<String> deactivateModule(@PathVariable String code) {
        return moduleService.deactivateModule(code);
    }
}
