package com.zynolo_nexus.auth_service.service.impl;

import com.zynolo_nexus.auth_service.exception.BadRequestException;
import com.zynolo_nexus.auth_service.exception.NotFoundException;
import com.zynolo_nexus.auth_service.model.Page;
import com.zynolo_nexus.auth_service.model.PageTask;
import com.zynolo_nexus.auth_service.repository.PageRepository;
import com.zynolo_nexus.auth_service.repository.PageTaskRepository;
import com.zynolo_nexus.auth_service.service.PageTaskManagementService;
import com.zynolo_nexus.contracts.pages.PageTaskDto;
import com.zynolo_nexus.contracts.pages.PageTaskRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class PageTaskManagementServiceImpl implements PageTaskManagementService {

    private final PageRepository pageRepository;
    private final PageTaskRepository pageTaskRepository;

    @Override
    public PageTaskDto createTask(PageTaskRequest request) {
        validate(request);

        Page page = pageRepository.findByCode(request.getPageCode())
                .orElseThrow(() -> new NotFoundException("page.notfound"));

        pageTaskRepository.findByPageAndCode(page, request.getCode())
                .ifPresent(t -> { throw new BadRequestException("page.task.code.exists"); });

        PageTask task = PageTask.builder()
                .page(page)
                .code(request.getCode())
                .name(request.getName())
                .description(request.getDescription())
                .sortOrder(request.getSortOrder())
                .active(true)
                .build();

        PageTask saved = pageTaskRepository.save(task);
        return toDto(saved);
    }

    @Override
    public PageTaskDto updateTask(String pageCode, String taskCode, PageTaskRequest request) {
        validate(request);
        if (StringUtils.hasText(request.getPageCode()) && !pageCode.equals(request.getPageCode())) {
            throw new BadRequestException("page.task.invalid");
        }
        if (StringUtils.hasText(request.getCode()) && !taskCode.equals(request.getCode())) {
            throw new BadRequestException("page.task.invalid");
        }

        Page page = pageRepository.findByCode(pageCode)
                .orElseThrow(() -> new NotFoundException("page.notfound"));

        PageTask task = pageTaskRepository.findByPageAndCode(page, taskCode)
                .orElseThrow(() -> new NotFoundException("page.task.notfound"));

        task.setName(request.getName());
        task.setDescription(request.getDescription());
        task.setSortOrder(request.getSortOrder());

        PageTask saved = pageTaskRepository.save(task);
        return toDto(saved);
    }

    @Override
    public List<PageTaskDto> getAllTasks(String pageCode) {
        if (StringUtils.hasText(pageCode)) {
            Page page = pageRepository.findByCode(pageCode)
                    .orElseThrow(() -> new NotFoundException("page.notfound"));
            return pageTaskRepository.findAllByPageAndActiveTrueOrderBySortOrderAsc(page)
                    .stream()
                    .map(this::toDto)
                    .collect(Collectors.toList());
        }

        return pageTaskRepository.findAllByActiveTrueOrderBySortOrderAsc()
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public void deactivateTask(String pageCode, String taskCode) {
        Page page = pageRepository.findByCode(pageCode)
                .orElseThrow(() -> new NotFoundException("page.notfound"));
        PageTask task = pageTaskRepository.findByPageAndCode(page, taskCode)
                .orElseThrow(() -> new NotFoundException("page.task.notfound"));
        task.setActive(false);
        pageTaskRepository.save(task);
    }

    private void validate(PageTaskRequest request) {
        if (request == null
                || !StringUtils.hasText(request.getPageCode())
                || !StringUtils.hasText(request.getCode())
                || !StringUtils.hasText(request.getName())) {
            throw new BadRequestException("page.task.invalid");
        }
    }

    private PageTaskDto toDto(PageTask task) {
        return PageTaskDto.builder()
                .id(task.getId())
                .pageCode(task.getPage().getCode())
                .code(task.getCode())
                .name(task.getName())
                .description(task.getDescription())
                .active(Boolean.TRUE.equals(task.getActive()))
                .sortOrder(task.getSortOrder())
                .build();
    }
}
