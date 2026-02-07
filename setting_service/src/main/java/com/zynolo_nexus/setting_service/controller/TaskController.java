package com.zynolo_nexus.setting_service.controller;

import com.zynolo_nexus.contracts.pages.TaskDto;
import com.zynolo_nexus.contracts.pages.TaskRequest;
import com.zynolo_nexus.setting_service.dto.api.MessageResponseDTO;
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

    @PostMapping("/{code}/get")
    public MessageResponseDTO<TaskDto> getTask(@PathVariable String code) {
        return taskService.getTask(code);
    }

    @PostMapping("/list")
    public MessageResponseDTO<List<TaskDto>> getAllTasks() {
        return taskService.getAllTasks();
    }

    @PostMapping("/{code}/deactivate")
    public MessageResponseDTO<String> deactivateTask(@PathVariable String code) {
        return taskService.deactivateTask(code);
    }
}
