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
import com.zynolo_nexus.setting_service.dto.request.ModuleFilterRequest;
import com.zynolo_nexus.setting_service.dto.request.ModuleReferenceDataRequest;
import com.zynolo_nexus.setting_service.dto.request.ModuleStatusUpdateRequest;
import com.zynolo_nexus.setting_service.dto.request.ModuleUpdateRequest;
import com.zynolo_nexus.setting_service.dto.request.ModuleViewRequest;
import com.zynolo_nexus.setting_service.dto.response.ModuleFilterResultDto;
import com.zynolo_nexus.setting_service.dto.response.ModuleReferenceDataDto;
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

    @PostMapping("/{code}/update")
    public MessageResponseDTO<ModuleDto> updateModule(
            @PathVariable String code,
            @RequestBody ModuleRequest request) {
        return moduleService.updateModule(code, request);
    }

    @PostMapping("/update")
    public MessageResponseDTO<ModuleDto> updateModule(@RequestBody ModuleUpdateRequest request) {
        String code = request != null ? request.getCode() : null;
        return moduleService.updateModule(code, request);
    }

    @PostMapping("/view")
    public MessageResponseDTO<ModuleDto> viewModule(@RequestBody ModuleViewRequest request) {
        Long id = request != null ? request.getId() : null;
        return moduleService.getModule(id);
    }

    @PostMapping("/status")
    public MessageResponseDTO<ModuleDto> updateStatus(@RequestBody ModuleStatusUpdateRequest request) {
        Long id = request != null ? request.getId() : null;
        return moduleService.updateModuleStatus(
                id,
                request != null ? request.getStatus() : null,
                request != null ? request.getUsername() : null
        );
    }

    @PostMapping("/list")
    public MessageResponseDTO<List<ModuleDto>> getAllModules() {
        return moduleService.getAllModules();
    }

    @PostMapping("/reference-data")
    public MessageResponseDTO<ModuleReferenceDataDto> referenceData(
            @RequestBody(required = false) ModuleReferenceDataRequest request) {
        return moduleService.getReferenceData(request);
    }

    @PostMapping("/filter-list")
    public MessageResponseDTO<ModuleFilterResultDto> filterList(@RequestBody ModuleFilterRequest request) {
        return moduleService.filterList(request);
    }

    @PostMapping("/{code}/deactivate")
    public MessageResponseDTO<String> deactivateModule(@PathVariable String code) {
        return moduleService.deactivateModule(code);
    }
}
