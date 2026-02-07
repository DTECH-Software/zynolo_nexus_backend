package com.zynolo_nexus.auth_service.controller.internal;

import com.zynolo_nexus.auth_service.service.TaskManagementService;
import com.zynolo_nexus.contracts.pages.TaskDto;
import com.zynolo_nexus.contracts.pages.TaskRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/internal/tasks")
@RequiredArgsConstructor
public class TaskInternalController {

    private final TaskManagementService taskManagementService;

    @PostMapping
    public TaskDto createTask(@RequestBody TaskRequest request) {
        return taskManagementService.createTask(request);
    }

    @PostMapping("/{code}/update")
    public TaskDto updateTask(@PathVariable String code, @RequestBody TaskRequest request) {
        return taskManagementService.updateTask(code, request);
    }

    @PostMapping("/{code}/get")
    public TaskDto getTask(@PathVariable String code) {
        return taskManagementService.getTask(code);
    }

    @PostMapping("/list")
    public List<TaskDto> getAllTasks() {
        return taskManagementService.getAllTasks();
    }

    @PostMapping("/{code}/deactivate")
    public void deactivateTask(@PathVariable String code) {
        taskManagementService.deactivateTask(code);
    }
}
