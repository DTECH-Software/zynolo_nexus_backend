package com.zynolo_nexus.auth_service.service.impl;

import com.zynolo_nexus.auth_service.exception.BadRequestException;
import com.zynolo_nexus.auth_service.exception.NotFoundException;
import com.zynolo_nexus.auth_service.model.Task;
import com.zynolo_nexus.auth_service.repository.TaskRepository;
import com.zynolo_nexus.auth_service.service.TaskManagementService;
import com.zynolo_nexus.contracts.pages.PageStatus;
import com.zynolo_nexus.contracts.pages.TaskDto;
import com.zynolo_nexus.contracts.pages.TaskRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class TaskManagementServiceImpl implements TaskManagementService {

    private final TaskRepository taskRepository;

    @Override
    public TaskDto createTask(TaskRequest request) {
        validateCreate(request);

        taskRepository.findByCode(request.getCode())
                .ifPresent(t -> { throw new BadRequestException("task.code.exists"); });

        Task task = Task.builder()
                .code(request.getCode())
                .name(request.getName())
                .description(request.getDescription())
                .sortOrder(request.getSortOrder())
                .active(resolveActive(request.getStatus()))
                .build();

        Task saved = taskRepository.save(task);
        return toDto(saved);
    }

    @Override
    public TaskDto updateTask(String code, TaskRequest request) {
        if (request == null) {
            throw new BadRequestException("task.invalid");
        }

        Task task = taskRepository.findByCode(code)
                .orElseThrow(() -> new NotFoundException("task.notfound"));

        if (StringUtils.hasText(request.getName())) {
            task.setName(request.getName());
        }
        if (StringUtils.hasText(request.getDescription())) {
            task.setDescription(request.getDescription());
        }
        if (request.getSortOrder() != null) {
            task.setSortOrder(request.getSortOrder());
        }
        if (request.getStatus() != null) {
            task.setActive(resolveActive(request.getStatus()));
        }

        Task saved = taskRepository.save(task);
        return toDto(saved);
    }

    @Override
    public TaskDto getTask(String code) {
        Task task = taskRepository.findByCode(code)
                .orElseThrow(() -> new NotFoundException("task.notfound"));
        return toDto(task);
    }

    @Override
    public List<TaskDto> getAllTasks() {
        return taskRepository.findAllByActiveTrueOrderBySortOrderAsc()
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<TaskDto> getAllTasksAll() {
        return taskRepository.findAllByOrderBySortOrderAsc()
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public void deactivateTask(String code) {
        Task task = taskRepository.findByCode(code)
                .orElseThrow(() -> new NotFoundException("task.notfound"));
        task.setActive(false);
        taskRepository.save(task);
    }

    private void validateCreate(TaskRequest request) {
        if (request == null
                || !StringUtils.hasText(request.getCode())
                || !StringUtils.hasText(request.getName())) {
            throw new BadRequestException("task.invalid");
        }
    }

    private boolean resolveActive(PageStatus status) {
        if (status == null) {
            return true;
        }
        return PageStatus.ACTIVE == status;
    }

    private TaskDto toDto(Task task) {
        return TaskDto.builder()
                .id(task.getId())
                .code(task.getCode())
                .name(task.getName())
                .description(task.getDescription())
                .active(Boolean.TRUE.equals(task.getActive()))
                .status(Boolean.TRUE.equals(task.getActive()) ? "ACTIVE" : "INACTIVE")
                .statusDescription(Boolean.TRUE.equals(task.getActive()) ? "Active" : "Inactive")
                .sortOrder(task.getSortOrder())
                .createdDate(task.getCreatedDate())
                .lastModifiedDate(task.getLastModifiedDate())
                .createdBy(task.getCreatedBy())
                .lastModifiedBy(task.getLastModifiedBy())
                .build();
    }
}
