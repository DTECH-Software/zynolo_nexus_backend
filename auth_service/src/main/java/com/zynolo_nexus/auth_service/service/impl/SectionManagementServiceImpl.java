package com.zynolo_nexus.auth_service.service.impl;

import com.zynolo_nexus.auth_service.exception.BadRequestException;
import com.zynolo_nexus.auth_service.exception.NotFoundException;
import com.zynolo_nexus.auth_service.model.Module;
import com.zynolo_nexus.auth_service.model.Section;
import com.zynolo_nexus.auth_service.repository.ModuleRepository;
import com.zynolo_nexus.auth_service.repository.SectionRepository;
import com.zynolo_nexus.auth_service.service.SectionManagementService;
import com.zynolo_nexus.contracts.pages.SectionDto;
import com.zynolo_nexus.contracts.pages.SectionRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class SectionManagementServiceImpl implements SectionManagementService {

    private final SectionRepository sectionRepository;
    private final ModuleRepository moduleRepository;

    @Override
    public SectionDto createSection(SectionRequest request) {
        validate(request);

        sectionRepository.findByCode(request.getCode())
                .ifPresent(s -> { throw new BadRequestException("section.code.exists"); });

        Module module = moduleRepository.findByCode(request.getModuleCode())
                .orElseThrow(() -> new NotFoundException("module.notfound"));

        Section section = Section.builder()
                .module(module)
                .code(request.getCode())
                .name(request.getName())
                .description(request.getDescription())
                .sortOrder(request.getSortOrder())
                .active(true)
                .build();

        Section saved = sectionRepository.save(section);
        return toDto(saved);
    }

    @Override
    public SectionDto updateSection(String code, SectionRequest request) {
        validate(request);

        Section section = sectionRepository.findByCode(code)
                .orElseThrow(() -> new NotFoundException("section.notfound"));

        Module module = moduleRepository.findByCode(request.getModuleCode())
                .orElseThrow(() -> new NotFoundException("module.notfound"));

        section.setModule(module);
        section.setName(request.getName());
        section.setDescription(request.getDescription());
        section.setSortOrder(request.getSortOrder());

        Section saved = sectionRepository.save(section);
        return toDto(saved);
    }

    @Override
    public List<SectionDto> getAllSections(String moduleCode) {
        if (StringUtils.hasText(moduleCode)) {
            Module module = moduleRepository.findByCode(moduleCode)
                    .orElseThrow(() -> new NotFoundException("module.notfound"));
            return sectionRepository.findAllByModuleAndActiveTrueOrderBySortOrderAsc(module)
                    .stream()
                    .map(this::toDto)
                    .collect(Collectors.toList());
        }

        return sectionRepository.findAllByActiveTrueOrderBySortOrderAsc()
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public void deactivateSection(String code) {
        Section section = sectionRepository.findByCode(code)
                .orElseThrow(() -> new NotFoundException("section.notfound"));
        section.setActive(false);
        sectionRepository.save(section);
    }

    private void validate(SectionRequest request) {
        if (request == null
                || !StringUtils.hasText(request.getModuleCode())
                || !StringUtils.hasText(request.getCode())
                || !StringUtils.hasText(request.getName())) {
            throw new BadRequestException("section.invalid");
        }
    }

    private SectionDto toDto(Section section) {
        return SectionDto.builder()
                .id(section.getId())
                .moduleCode(section.getModule().getCode())
                .code(section.getCode())
                .name(section.getName())
                .description(section.getDescription())
                .active(Boolean.TRUE.equals(section.getActive()))
                .sortOrder(section.getSortOrder())
                .build();
    }
}
