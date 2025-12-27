package com.zynolo_nexus.setting_service.service;

import com.zynolo_nexus.contracts.pages.SectionDto;
import com.zynolo_nexus.contracts.pages.SectionRequest;
import com.zynolo_nexus.setting_service.dto.api.MessageResponseDTO;

import java.util.List;

public interface SectionService {

    MessageResponseDTO<SectionDto> createSection(SectionRequest request);

    MessageResponseDTO<SectionDto> updateSection(String code, SectionRequest request);

    MessageResponseDTO<List<SectionDto>> getAllSections();

    MessageResponseDTO<String> deactivateSection(String code);
}
