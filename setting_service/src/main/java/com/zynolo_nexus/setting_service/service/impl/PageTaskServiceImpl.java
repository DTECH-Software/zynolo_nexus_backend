package com.zynolo_nexus.setting_service.service.impl;

import com.zynolo_nexus.contracts.pages.PageTaskDto;
import com.zynolo_nexus.contracts.pages.PageTaskRequest;
import com.zynolo_nexus.setting_service.client.AuthModuleClient;
import com.zynolo_nexus.setting_service.dto.api.MessageResponseDTO;
import com.zynolo_nexus.setting_service.service.PageTaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PageTaskServiceImpl implements PageTaskService {

    private final AuthModuleClient authModuleClient;

    @Override
    public MessageResponseDTO<PageTaskDto> createTask(PageTaskRequest request) {
        PageTaskDto task = authModuleClient.createTask(request);
        return MessageResponseDTO.<PageTaskDto>builder()
                .success(true)
                .message("Page task created successfully")
                .data(task)
                .errors(null)
                .errorCode(0)
                .responseTime(LocalDateTime.now())
                .build();
    }

    @Override
    public MessageResponseDTO<PageTaskDto> updateTask(String pageCode, String taskCode, PageTaskRequest request) {
        PageTaskDto task = authModuleClient.updateTask(pageCode, taskCode, request);
        return MessageResponseDTO.<PageTaskDto>builder()
                .success(true)
                .message("Page task updated successfully")
                .data(task)
                .errors(null)
                .errorCode(0)
                .responseTime(LocalDateTime.now())
                .build();
    }

    @Override
    public MessageResponseDTO<List<PageTaskDto>> getAllTasks() {
        List<PageTaskDto> tasks = authModuleClient.getAllTasks();
        return MessageResponseDTO.<List<PageTaskDto>>builder()
                .success(true)
                .message("Page tasks loaded successfully")
                .data(tasks)
                .errors(null)
                .errorCode(0)
                .responseTime(LocalDateTime.now())
                .build();
    }

    @Override
    public MessageResponseDTO<String> deactivateTask(String pageCode, String taskCode) {
        authModuleClient.deactivateTask(pageCode, taskCode);
        return MessageResponseDTO.<String>builder()
                .success(true)
                .message("Page task deactivated successfully")
                .data(taskCode)
                .errors(null)
                .errorCode(0)
                .responseTime(LocalDateTime.now())
                .build();
    }
}
