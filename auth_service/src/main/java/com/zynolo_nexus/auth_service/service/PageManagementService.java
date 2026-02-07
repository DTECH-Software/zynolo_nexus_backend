package com.zynolo_nexus.auth_service.service;

import com.zynolo_nexus.contracts.pages.PageDto;
import com.zynolo_nexus.contracts.pages.PageRequest;

import java.util.List;

public interface PageManagementService {

    PageDto createPage(PageRequest request);

    PageDto updatePage(String code, PageRequest request);

    List<PageDto> getAllPages(String sectionCode);

    List<PageDto> getAllPagesIncludingInactive(String sectionCode);

    PageDto getPage(String code);

    PageDto updatePageStatus(String code, com.zynolo_nexus.contracts.pages.PageStatus status);

    void deactivatePage(String code);
}
