package com.zynolo_nexus.auth_service.service;

import com.zynolo_nexus.contracts.pages.SectionDto;
import com.zynolo_nexus.contracts.pages.SectionRequest;

import java.util.List;

public interface SectionManagementService {

    SectionDto createSection(SectionRequest request);

    SectionDto updateSection(String code, SectionRequest request);

    List<SectionDto> getAllSections(String moduleCode);

    void deactivateSection(String code);
}
