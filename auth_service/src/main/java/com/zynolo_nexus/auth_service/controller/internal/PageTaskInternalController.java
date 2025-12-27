package com.zynolo_nexus.auth_service.controller.internal;

import com.zynolo_nexus.auth_service.service.PageTaskManagementService;
import com.zynolo_nexus.contracts.pages.PageTaskDto;
import com.zynolo_nexus.contracts.pages.PageTaskRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/internal/page-tasks")
@RequiredArgsConstructor
public class PageTaskInternalController {

    private final PageTaskManagementService pageTaskManagementService;

    @PostMapping
    public PageTaskDto createTask(@RequestBody PageTaskRequest request) {
        return pageTaskManagementService.createTask(request);
    }

    @PutMapping("/{pageCode}/{taskCode}")
    public PageTaskDto updateTask(@PathVariable String pageCode,
                                  @PathVariable String taskCode,
                                  @RequestBody PageTaskRequest request) {
        return pageTaskManagementService.updateTask(pageCode, taskCode, request);
    }

    @GetMapping
    public List<PageTaskDto> getAllTasks(@RequestParam(required = false) String pageCode) {
        return pageTaskManagementService.getAllTasks(pageCode);
    }

    @DeleteMapping("/{pageCode}/{taskCode}")
    public void deactivateTask(@PathVariable String pageCode, @PathVariable String taskCode) {
        pageTaskManagementService.deactivateTask(pageCode, taskCode);
    }
}
