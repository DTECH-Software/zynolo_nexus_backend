package com.zynolo_nexus.auth_service.service.impl;

import com.zynolo_nexus.auth_service.exception.BadRequestException;
import com.zynolo_nexus.auth_service.exception.NotFoundException;
import com.zynolo_nexus.auth_service.model.Page;
import com.zynolo_nexus.auth_service.model.Section;
import com.zynolo_nexus.auth_service.repository.PageRepository;
import com.zynolo_nexus.auth_service.repository.SectionRepository;
import com.zynolo_nexus.auth_service.service.PageManagementService;
import com.zynolo_nexus.contracts.pages.PageDto;
import com.zynolo_nexus.contracts.pages.PageRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PageManagementServiceImpl implements PageManagementService {

    private final PageRepository pageRepository;
    private final SectionRepository sectionRepository;

    @Override
    public PageDto createPage(PageRequest request) {
        validate(request);

        Section section = sectionRepository.findByCode(request.getSectionCode())
                .orElseThrow(() -> new NotFoundException("section.notfound"));

        pageRepository.findByCode(request.getCode())
                .ifPresent(p -> { throw new BadRequestException("page.code.exists"); });

        Page page = Page.builder()
                .section(section)
                .code(request.getCode())
                .name(request.getName())
                .description(request.getDescription())
                .sortOrder(request.getSortOrder())
                .active(true)
                .build();

        Page saved = pageRepository.save(page);
        return toDto(saved);
    }

    @Override
    public PageDto updatePage(String code, PageRequest request) {
        validate(request);

        Page page = pageRepository.findByCode(code)
                .orElseThrow(() -> new NotFoundException("page.notfound"));

        Section section = sectionRepository.findByCode(request.getSectionCode())
                .orElseThrow(() -> new NotFoundException("section.notfound"));

        page.setSection(section);
        page.setName(request.getName());
        page.setDescription(request.getDescription());
        page.setSortOrder(request.getSortOrder());

        Page saved = pageRepository.save(page);
        return toDto(saved);
    }

    @Override
    public List<PageDto> getAllPages(String sectionCode) {
        if (StringUtils.hasText(sectionCode)) {
            Section section = sectionRepository.findByCode(sectionCode)
                    .orElseThrow(() -> new NotFoundException("section.notfound"));
            return pageRepository.findAllBySectionAndActiveTrueOrderBySortOrderAsc(section)
                    .stream()
                    .map(this::toDto)
                    .collect(Collectors.toList());
        }

        return pageRepository.findAllByActiveTrueOrderBySortOrderAsc()
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public void deactivatePage(String code) {
        Page page = pageRepository.findByCode(code)
                .orElseThrow(() -> new NotFoundException("page.notfound"));
        page.setActive(false);
        pageRepository.save(page);
    }

    private void validate(PageRequest request) {
        if (request == null
                || !StringUtils.hasText(request.getSectionCode())
                || !StringUtils.hasText(request.getCode())
                || !StringUtils.hasText(request.getName())) {
            throw new BadRequestException("page.invalid");
        }
    }

    private PageDto toDto(Page page) {
        return PageDto.builder()
                .id(page.getId())
                .sectionCode(page.getSection().getCode())
                .code(page.getCode())
                .name(page.getName())
                .description(page.getDescription())
                .active(Boolean.TRUE.equals(page.getActive()))
                .sortOrder(page.getSortOrder())
                .build();
    }
}
