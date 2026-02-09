package com.zynolo_nexus.setting_service.controller;

import com.zynolo_nexus.contracts.pages.TaskDto;
import com.zynolo_nexus.contracts.pages.TaskRequest;
import com.zynolo_nexus.setting_service.dto.api.MessageResponseDTO;
import com.zynolo_nexus.setting_service.dto.request.TaskFilterRequest;
import com.zynolo_nexus.setting_service.dto.request.TaskReferenceDataRequest;
import com.zynolo_nexus.setting_service.dto.request.TaskStatusUpdateRequest;
import com.zynolo_nexus.setting_service.dto.request.TaskUpdateRequest;
import com.zynolo_nexus.setting_service.dto.request.TaskViewRequest;
import com.zynolo_nexus.setting_service.dto.response.TaskFilterResultDto;
import com.zynolo_nexus.setting_service.dto.response.TaskReferenceDataDto;
import com.zynolo_nexus.setting_service.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/setting/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    @PostMapping
    public MessageResponseDTO<TaskDto> createTask(@RequestBody TaskRequest request) {
        return taskService.createTask(request);
    }

    @PostMapping("/{code}/update")
    public MessageResponseDTO<TaskDto> updateTask(@PathVariable String code,
                                                  @RequestBody TaskRequest request) {
        return taskService.updateTask(code, request);
    }

    @PostMapping("/update")
    public MessageResponseDTO<TaskDto> updateTask(@RequestBody TaskUpdateRequest request) {
        String code = request != null ? request.getCode() : null;
        return taskService.updateTask(code, request);
    }

    @PostMapping("/view")
    public MessageResponseDTO<TaskDto> viewTask(@RequestBody TaskViewRequest request) {
        Long id = request != null ? request.getId() : null;
        return taskService.getTask(id);
    }

    @PostMapping("/status")
    public MessageResponseDTO<TaskDto> updateStatus(@RequestBody TaskStatusUpdateRequest request) {
        Long id = request != null ? request.getId() : null;
        return taskService.updateTaskStatus(
                id,
                request != null ? request.getStatus() : null,
                request != null ? request.getUsername() : null
        );
    }

    @PostMapping("/{code}/get")
    public MessageResponseDTO<TaskDto> getTask(@PathVariable String code) {
        return taskService.getTask(code);
    }

    @PostMapping("/list")
    public MessageResponseDTO<List<TaskDto>> getAllTasks() {
        return taskService.getAllTasks();
    }

    @PostMapping("/reference-data")
    public MessageResponseDTO<TaskReferenceDataDto> referenceData(
            @RequestBody(required = false) TaskReferenceDataRequest request) {
        return taskService.getReferenceData(request);
    }

    @PostMapping("/filter-list")
    public MessageResponseDTO<TaskFilterResultDto> filterList(@RequestBody TaskFilterRequest request) {
        return taskService.filterList(request);
    }

    @PostMapping("/{code}/deactivate")
    public MessageResponseDTO<String> deactivateTask(@PathVariable String code) {
        return taskService.deactivateTask(code);
    }
}
