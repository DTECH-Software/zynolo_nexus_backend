package com.zynolo_nexus.auth_service.service;

import com.zynolo_nexus.contracts.pages.PageTaskDto;
import com.zynolo_nexus.contracts.pages.PageTaskRequest;

import java.util.List;

public interface PageTaskManagementService {

    PageTaskDto createTask(PageTaskRequest request);

    PageTaskDto updateTask(String pageCode, String taskCode, PageTaskRequest request);

    List<PageTaskDto> getAllTasks(String pageCode);

    void deactivateTask(String pageCode, String taskCode);
}
