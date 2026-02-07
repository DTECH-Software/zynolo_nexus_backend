package com.zynolo_nexus.setting_service.service;

import com.zynolo_nexus.contracts.pages.SectionDto;
import com.zynolo_nexus.contracts.pages.SectionRequest;
import com.zynolo_nexus.contracts.pages.SectionStatus;
import com.zynolo_nexus.setting_service.dto.api.MessageResponseDTO;
import com.zynolo_nexus.setting_service.dto.request.SectionFilterRequest;
import com.zynolo_nexus.setting_service.dto.request.SectionReferenceDataRequest;
import com.zynolo_nexus.setting_service.dto.request.SectionUpdateRequest;
import com.zynolo_nexus.setting_service.dto.response.SectionFilterResultDto;
import com.zynolo_nexus.setting_service.dto.response.SectionReferenceDataDto;

import java.util.List;

public interface SectionService {

    MessageResponseDTO<SectionDto> createSection(SectionRequest request);

    MessageResponseDTO<SectionDto> updateSection(String code, SectionRequest request);

    MessageResponseDTO<SectionDto> updateSection(String code, SectionUpdateRequest request);

    MessageResponseDTO<SectionDto> getSection(Long id);

    MessageResponseDTO<SectionDto> updateSectionStatus(String code, SectionStatus status);

    MessageResponseDTO<List<SectionDto>> getAllSections();

    MessageResponseDTO<SectionReferenceDataDto> getReferenceData(SectionReferenceDataRequest request);

    MessageResponseDTO<SectionFilterResultDto> filterList(SectionFilterRequest request);

    MessageResponseDTO<String> deactivateSection(String code);
}
