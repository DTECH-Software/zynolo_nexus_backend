package com.zynolo_nexus.auth_service.service.impl;

import com.zynolo_nexus.auth_service.exception.BadRequestException;
import com.zynolo_nexus.auth_service.exception.NotFoundException;
import com.zynolo_nexus.auth_service.enums.ModuleStatus;
import com.zynolo_nexus.auth_service.model.Module;
import com.zynolo_nexus.auth_service.repository.ModuleRepository;
import com.zynolo_nexus.auth_service.service.ModuleManagementService;
import com.zynolo_nexus.contracts.modules.ModuleDto;
import com.zynolo_nexus.contracts.modules.ModuleRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ModuleManagementServiceImpl implements ModuleManagementService {

    private final ModuleRepository moduleRepository;

    @Override
    public ModuleDto createModule(ModuleRequest request) {
        validateCreate(request);

        moduleRepository.findByCode(request.getCode())
                .ifPresent(m -> { throw new BadRequestException("module.code.exists"); });

        Module module = Module.builder()
                .code(request.getCode())
                .name(request.getName())
                .description(request.getDescription())
                .url(request.getUrl())
                .status(resolveStatus(request.getStatus()))
                .sortOrder(request.getSortOrder())
                .build();

        Module saved = moduleRepository.save(module);
        return toDto(saved);
    }

    @Override
    public ModuleDto updateModule(String code, ModuleRequest request) {
        validateUpdate(request);

        Module module = moduleRepository.findByCode(code)
                .orElseThrow(() -> new NotFoundException("module.notfound"));

        if (StringUtils.hasText(request.getName())) {
            module.setName(request.getName());
        }
        if (StringUtils.hasText(request.getDescription())) {
            module.setDescription(request.getDescription());
        }
        if (StringUtils.hasText(request.getUrl())) {
            module.setUrl(request.getUrl());
        }
        if (request.getStatus() != null) {
            module.setStatus(resolveStatus(request.getStatus()));
        }
        if (request.getSortOrder() != null) {
            module.setSortOrder(request.getSortOrder());
        }

        Module saved = moduleRepository.save(module);
        return toDto(saved);
    }

    @Override
    public List<ModuleDto> getAllModules() {
        return moduleRepository.findAllActiveOrderBySortOrderAsc(ModuleStatus.ACTIVE)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public void deactivateModule(String code) {
        Module module = moduleRepository.findByCode(code)
                .orElseThrow(() -> new NotFoundException("module.notfound"));

        module.setStatus(ModuleStatus.DEACTIVE);
        moduleRepository.save(module);
    }

    private void validateCreate(ModuleRequest request) {
        if (request == null || !StringUtils.hasText(request.getCode()) || !StringUtils.hasText(request.getName())) {
            throw new BadRequestException("module.invalid");
        }
    }

    private void validateUpdate(ModuleRequest request) {
        if (request == null) {
            throw new BadRequestException("module.invalid");
        }
        boolean hasAny =
                StringUtils.hasText(request.getName()) ||
                StringUtils.hasText(request.getDescription()) ||
                StringUtils.hasText(request.getUrl()) ||
                request.getStatus() != null ||
                request.getSortOrder() != null;
        if (!hasAny) {
            throw new BadRequestException("module.invalid");
        }
    }

    private ModuleDto toDto(Module module) {
        return ModuleDto.builder()
                .id(module.getId())
                .code(module.getCode())
                .name(module.getName())
                .description(module.getDescription())
                .url(module.getUrl())
                .status(toContractStatus(module.getStatus()))
                .sortOrder(module.getSortOrder())
                .build();
    }

    private ModuleStatus resolveStatus(com.zynolo_nexus.contracts.modules.ModuleStatus status) {
        if (status == null) {
            return ModuleStatus.ACTIVE;
        }
        return ModuleStatus.valueOf(status.name());
    }

    private com.zynolo_nexus.contracts.modules.ModuleStatus toContractStatus(ModuleStatus status) {
        if (status == null) {
            return com.zynolo_nexus.contracts.modules.ModuleStatus.ACTIVE;
        }
        return com.zynolo_nexus.contracts.modules.ModuleStatus.valueOf(status.name());
    }
}
