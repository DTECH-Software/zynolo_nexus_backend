package com.zynolo_nexus.setting_service.service;

import com.zynolo_nexus.contracts.pages.TaskDto;
import com.zynolo_nexus.contracts.pages.TaskRequest;
import com.zynolo_nexus.setting_service.dto.api.MessageResponseDTO;
import com.zynolo_nexus.setting_service.dto.request.TaskFilterRequest;
import com.zynolo_nexus.setting_service.dto.request.TaskReferenceDataRequest;
import com.zynolo_nexus.setting_service.dto.request.TaskUpdateRequest;
import com.zynolo_nexus.setting_service.dto.response.TaskFilterResultDto;
import com.zynolo_nexus.setting_service.dto.response.TaskReferenceDataDto;

import java.util.List;

public interface TaskService {

    MessageResponseDTO<TaskDto> createTask(TaskRequest request);

    MessageResponseDTO<TaskDto> updateTask(String code, TaskRequest request);

    MessageResponseDTO<TaskDto> updateTask(String code, TaskUpdateRequest request);

    MessageResponseDTO<TaskDto> getTask(Long id);

    MessageResponseDTO<TaskDto> updateTaskStatus(Long id, com.zynolo_nexus.contracts.pages.PageStatus status, String username);

    MessageResponseDTO<TaskDto> getTask(String code);

    MessageResponseDTO<List<TaskDto>> getAllTasks();

    MessageResponseDTO<TaskReferenceDataDto> getReferenceData(TaskReferenceDataRequest request);

    MessageResponseDTO<TaskFilterResultDto> filterList(TaskFilterRequest request);

    MessageResponseDTO<String> deactivateTask(String code);
}
