package com.zynolo_nexus.setting_service.service.impl;

import com.zynolo_nexus.contracts.pages.TaskDto;
import com.zynolo_nexus.contracts.pages.TaskRequest;
import com.zynolo_nexus.setting_service.client.AuthModuleClient;
import com.zynolo_nexus.setting_service.dto.api.MessageResponseDTO;
import com.zynolo_nexus.setting_service.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

    private final AuthModuleClient authModuleClient;

    @Override
    public MessageResponseDTO<TaskDto> createTask(TaskRequest request) {
        TaskDto task = authModuleClient.createTask(request);
        return MessageResponseDTO.<TaskDto>builder()
                .success(true)
                .message("Task created successfully")
                .data(task)
                .errors(null)
                .errorCode(0)
                .responseTime(LocalDateTime.now())
                .build();
    }

    @Override
    public MessageResponseDTO<TaskDto> updateTask(String code, TaskRequest request) {
        TaskDto task = authModuleClient.updateTask(code, request);
        return MessageResponseDTO.<TaskDto>builder()
                .success(true)
                .message("Task updated successfully")
                .data(task)
                .errors(null)
                .errorCode(0)
                .responseTime(LocalDateTime.now())
                .build();
    }

    @Override
    public MessageResponseDTO<TaskDto> getTask(String code) {
        TaskDto task = authModuleClient.getTask(code);
        return MessageResponseDTO.<TaskDto>builder()
                .success(true)
                .message("Task details retrieved successfully")
                .data(task)
                .errors(null)
                .errorCode(0)
                .responseTime(LocalDateTime.now())
                .build();
    }

    @Override
    public MessageResponseDTO<List<TaskDto>> getAllTasks() {
        List<TaskDto> tasks = authModuleClient.getAllTasksCatalog();
        return MessageResponseDTO.<List<TaskDto>>builder()
                .success(true)
                .message("Tasks loaded successfully")
                .data(tasks)
                .errors(null)
                .errorCode(0)
                .responseTime(LocalDateTime.now())
                .build();
    }

    @Override
    public MessageResponseDTO<String> deactivateTask(String code) {
        authModuleClient.deactivateTask(code);
        return MessageResponseDTO.<String>builder()
                .success(true)
                .message("Task deactivated successfully")
                .data(code)
                .errors(null)
                .errorCode(0)
                .responseTime(LocalDateTime.now())
                .build();
    }
}
