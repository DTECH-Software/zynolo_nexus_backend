package com.zynolo_nexus.auth_service.service;

import com.zynolo_nexus.contracts.pages.SectionDto;
import com.zynolo_nexus.contracts.pages.SectionRequest;
import com.zynolo_nexus.contracts.pages.SectionStatus;

import java.util.List;

public interface SectionManagementService {

    SectionDto createSection(SectionRequest request);

    SectionDto updateSection(String code, SectionRequest request);

    List<SectionDto> getAllSections(String moduleCode);

    List<SectionDto> getAllSectionsAll();

    SectionDto updateSectionStatus(String code, SectionStatus status);

    void deactivateSection(String code);
}
