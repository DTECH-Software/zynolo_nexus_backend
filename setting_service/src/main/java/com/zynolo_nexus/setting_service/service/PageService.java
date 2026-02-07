package com.zynolo_nexus.setting_service.service;

import com.zynolo_nexus.contracts.pages.PageDto;
import com.zynolo_nexus.contracts.pages.PageRequest;
import com.zynolo_nexus.setting_service.dto.api.MessageResponseDTO;
import com.zynolo_nexus.setting_service.dto.request.PageFilterRequest;
import com.zynolo_nexus.setting_service.dto.request.PageReferenceDataRequest;
import com.zynolo_nexus.setting_service.dto.request.PageUpdateRequest;
import com.zynolo_nexus.setting_service.dto.response.PageFilterResultDto;
import com.zynolo_nexus.setting_service.dto.response.PageReferenceDataDto;

import java.util.List;

public interface PageService {

    MessageResponseDTO<PageDto> createPage(PageRequest request);

    MessageResponseDTO<PageDto> updatePage(String code, PageUpdateRequest request);

    MessageResponseDTO<List<PageDto>> getAllPages();

    MessageResponseDTO<PageDto> getPage(String code);

    MessageResponseDTO<PageDto> updatePageStatus(String code, com.zynolo_nexus.contracts.pages.PageStatus status);

    MessageResponseDTO<PageReferenceDataDto> getReferenceData(PageReferenceDataRequest request);

    MessageResponseDTO<PageFilterResultDto> filterList(PageFilterRequest request);

    MessageResponseDTO<String> deactivatePage(String code);
}
