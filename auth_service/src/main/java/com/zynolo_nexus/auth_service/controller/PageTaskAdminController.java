package com.zynolo_nexus.auth_service.controller;

import com.zynolo_nexus.auth_service.dto.api.MessageResponseDTO;
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

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/page-tasks")
@RequiredArgsConstructor
public class PageTaskAdminController {

    private final PageTaskManagementService pageTaskManagementService;

    @PostMapping
    public MessageResponseDTO<PageTaskDto> create(@RequestBody PageTaskRequest request) {
        PageTaskDto task = pageTaskManagementService.createTask(request);
        return wrap(task, "page.task.create.success");
    }

    @PutMapping("/{pageCode}/{taskCode}")
    public MessageResponseDTO<PageTaskDto> update(@PathVariable String pageCode,
                                                  @PathVariable String taskCode,
                                                  @RequestBody PageTaskRequest request) {
        PageTaskDto task = pageTaskManagementService.updateTask(pageCode, taskCode, request);
        return wrap(task, "page.task.update.success");
    }

    @GetMapping
    public MessageResponseDTO<List<PageTaskDto>> getAll(@RequestParam(required = false) String pageCode) {
        List<PageTaskDto> tasks = pageTaskManagementService.getAllTasks(pageCode);
        return MessageResponseDTO.<List<PageTaskDto>>builder()
                .success(true)
                .message("page.task.fetch.success")
                .data(tasks)
                .errors(null)
                .errorCode(0)
                .responseTime(LocalDateTime.now())
                .build();
    }

    @DeleteMapping("/{pageCode}/{taskCode}")
    public MessageResponseDTO<String> deactivate(@PathVariable String pageCode,
                                                 @PathVariable String taskCode) {
        pageTaskManagementService.deactivateTask(pageCode, taskCode);
        return MessageResponseDTO.<String>builder()
                .success(true)
                .message("page.task.deactivate.success")
                .data("deactivated")
                .errors(null)
                .errorCode(0)
                .responseTime(LocalDateTime.now())
                .build();
    }

    private MessageResponseDTO<PageTaskDto> wrap(PageTaskDto dto, String message) {
        return MessageResponseDTO.<PageTaskDto>builder()
                .success(true)
                .message(message)
                .data(dto)
                .errors(null)
                .errorCode(0)
                .responseTime(LocalDateTime.now())
                .build();
    }
}
