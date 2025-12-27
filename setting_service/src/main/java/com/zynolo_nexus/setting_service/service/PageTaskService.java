package com.zynolo_nexus.setting_service.service;

import com.zynolo_nexus.contracts.pages.PageTaskDto;
import com.zynolo_nexus.contracts.pages.PageTaskRequest;
import com.zynolo_nexus.setting_service.dto.api.MessageResponseDTO;

import java.util.List;

public interface PageTaskService {

    MessageResponseDTO<PageTaskDto> createTask(PageTaskRequest request);

    MessageResponseDTO<PageTaskDto> updateTask(String pageCode, String taskCode, PageTaskRequest request);

    MessageResponseDTO<List<PageTaskDto>> getAllTasks();

    MessageResponseDTO<String> deactivateTask(String pageCode, String taskCode);
}
