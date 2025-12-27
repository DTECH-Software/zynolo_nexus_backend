package com.zynolo_nexus.setting_service.controller;

import com.zynolo_nexus.contracts.pages.PageTaskDto;
import com.zynolo_nexus.contracts.pages.PageTaskRequest;
import com.zynolo_nexus.setting_service.dto.api.MessageResponseDTO;
import com.zynolo_nexus.setting_service.service.PageTaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/setting/page-tasks")
@RequiredArgsConstructor
public class PageTaskController {

    private final PageTaskService pageTaskService;

    @PostMapping
    public MessageResponseDTO<PageTaskDto> createTask(@RequestBody PageTaskRequest request) {
        return pageTaskService.createTask(request);
    }

    @PutMapping("/{pageCode}/{taskCode}")
    public MessageResponseDTO<PageTaskDto> updateTask(@PathVariable String pageCode,
                                                      @PathVariable String taskCode,
                                                      @RequestBody PageTaskRequest request) {
        return pageTaskService.updateTask(pageCode, taskCode, request);
    }

    @GetMapping
    public MessageResponseDTO<List<PageTaskDto>> getAllTasks() {
        return pageTaskService.getAllTasks();
    }

    @DeleteMapping("/{pageCode}/{taskCode}")
    public MessageResponseDTO<String> deactivateTask(@PathVariable String pageCode,
                                                     @PathVariable String taskCode) {
        return pageTaskService.deactivateTask(pageCode, taskCode);
    }
}
