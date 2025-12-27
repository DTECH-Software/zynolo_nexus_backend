package com.zynolo_nexus.auth_service.service.impl;

import com.zynolo_nexus.auth_service.exception.BadRequestException;
import com.zynolo_nexus.auth_service.exception.NotFoundException;
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
        validate(request);

        moduleRepository.findByCode(request.getCode())
                .ifPresent(m -> { throw new BadRequestException("module.code.exists"); });

        Module module = Module.builder()
                .code(request.getCode())
                .name(request.getName())
                .description(request.getDescription())
                .sortOrder(request.getSortOrder())
                .active(true)
                .build();

        Module saved = moduleRepository.save(module);
        return toDto(saved);
    }

    @Override
    public ModuleDto updateModule(String code, ModuleRequest request) {
        validate(request);

        Module module = moduleRepository.findByCode(code)
                .orElseThrow(() -> new NotFoundException("module.notfound"));

        module.setName(request.getName());
        module.setDescription(request.getDescription());
        module.setSortOrder(request.getSortOrder());

        Module saved = moduleRepository.save(module);
        return toDto(saved);
    }

    @Override
    public List<ModuleDto> getAllModules() {
        return moduleRepository.findAllByActiveTrueOrderBySortOrderAsc()
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public void deactivateModule(String code) {
        Module module = moduleRepository.findByCode(code)
                .orElseThrow(() -> new NotFoundException("module.notfound"));

        module.setActive(false);
        moduleRepository.save(module);
    }

    private void validate(ModuleRequest request) {
        if (request == null || !StringUtils.hasText(request.getCode()) || !StringUtils.hasText(request.getName())) {
            throw new BadRequestException("module.invalid");
        }
    }

    private ModuleDto toDto(Module module) {
        return ModuleDto.builder()
                .id(module.getId())
                .code(module.getCode())
                .name(module.getName())
                .description(module.getDescription())
                .active(Boolean.TRUE.equals(module.getActive()))
                .sortOrder(module.getSortOrder())
                .build();
    }
}
