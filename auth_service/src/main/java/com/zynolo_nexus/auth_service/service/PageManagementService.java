package com.zynolo_nexus.auth_service.service;

import com.zynolo_nexus.contracts.pages.PageDto;
import com.zynolo_nexus.contracts.pages.PageRequest;

import java.util.List;

public interface PageManagementService {

    PageDto createPage(PageRequest request);

    PageDto updatePage(String code, PageRequest request);

    List<PageDto> getAllPages(String sectionCode);

    void deactivatePage(String code);
}
