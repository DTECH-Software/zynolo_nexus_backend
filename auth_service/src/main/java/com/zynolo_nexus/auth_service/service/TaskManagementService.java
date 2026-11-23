package com.zynolo_nexus.auth_service.service;

import com.zynolo_nexus.contracts.pages.TaskDto;
import com.zynolo_nexus.contracts.pages.TaskRequest;

import java.util.List;

public interface TaskManagementService {

    TaskDto createTask(TaskRequest request);

    TaskDto updateTask(String code, TaskRequest request);

    TaskDto getTask(String code);

    List<TaskDto> getAllTasks();

    List<TaskDto> getAllTasksAll();

    void deactivateTask(String code);
}
