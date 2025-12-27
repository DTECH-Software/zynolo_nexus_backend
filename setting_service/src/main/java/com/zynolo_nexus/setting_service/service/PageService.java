package com.zynolo_nexus.setting_service.service;

import com.zynolo_nexus.contracts.pages.PageDto;
import com.zynolo_nexus.contracts.pages.PageRequest;
import com.zynolo_nexus.setting_service.dto.api.MessageResponseDTO;

import java.util.List;

public interface PageService {

    MessageResponseDTO<PageDto> createPage(PageRequest request);

    MessageResponseDTO<PageDto> updatePage(String code, PageRequest request);

    MessageResponseDTO<List<PageDto>> getAllPages();

    MessageResponseDTO<String> deactivatePage(String code);
}
