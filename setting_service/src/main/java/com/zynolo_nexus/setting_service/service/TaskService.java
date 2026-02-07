package com.zynolo_nexus.setting_service.service;

import com.zynolo_nexus.contracts.pages.TaskDto;
import com.zynolo_nexus.contracts.pages.TaskRequest;
import com.zynolo_nexus.setting_service.dto.api.MessageResponseDTO;

import java.util.List;

public interface TaskService {

    MessageResponseDTO<TaskDto> createTask(TaskRequest request);

    MessageResponseDTO<TaskDto> updateTask(String code, TaskRequest request);

    MessageResponseDTO<TaskDto> getTask(String code);

    MessageResponseDTO<List<TaskDto>> getAllTasks();

    MessageResponseDTO<String> deactivateTask(String code);
}
