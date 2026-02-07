package com.zynolo_nexus.setting_service.controller;

import com.zynolo_nexus.contracts.pages.SectionDto;
import com.zynolo_nexus.contracts.pages.SectionRequest;
import com.zynolo_nexus.contracts.pages.SectionStatusRequest;
import com.zynolo_nexus.setting_service.dto.api.MessageResponseDTO;
import com.zynolo_nexus.setting_service.dto.request.SectionFilterRequest;
import com.zynolo_nexus.setting_service.dto.request.SectionReferenceDataRequest;
import com.zynolo_nexus.setting_service.dto.request.SectionStatusUpdateRequest;
import com.zynolo_nexus.setting_service.dto.request.SectionUpdateRequest;
import com.zynolo_nexus.setting_service.dto.request.SectionViewRequest;
import com.zynolo_nexus.setting_service.dto.response.SectionFilterResultDto;
import com.zynolo_nexus.setting_service.dto.response.SectionReferenceDataDto;
import com.zynolo_nexus.setting_service.service.SectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/setting/sections")
@RequiredArgsConstructor
public class SectionController {

    private final SectionService sectionService;

    @PostMapping
    public MessageResponseDTO<SectionDto> createSection(@RequestBody SectionRequest request) {
        return sectionService.createSection(request);
    }

    @PostMapping("/{code}/update")
    public MessageResponseDTO<SectionDto> updateSection(@PathVariable String code,
                                                        @RequestBody SectionRequest request) {
        return sectionService.updateSection(code, request);
    }

    @PostMapping("/update")
    public MessageResponseDTO<SectionDto> updateSection(@RequestBody SectionUpdateRequest request) {
        String code = request != null ? request.getCode() : null;
        return sectionService.updateSection(code, request);
    }

    @PostMapping("/view")
    public MessageResponseDTO<SectionDto> viewSection(@RequestBody SectionViewRequest request) {
        Long id = request != null ? request.getId() : null;
        return sectionService.getSection(id);
    }

    @PostMapping("/status")
    public MessageResponseDTO<SectionDto> updateStatus(@RequestBody SectionStatusUpdateRequest request) {
        Long id = request != null ? request.getId() : null;
        return sectionService.updateSectionStatus(
                id,
                request != null ? request.getStatus() : null,
                request != null ? request.getUsername() : null
        );
    }

    @PostMapping("/{id}/status")
    public MessageResponseDTO<SectionDto> updateStatus(@PathVariable Long id,
                                                       @RequestBody SectionStatusRequest request) {
        return sectionService.updateSectionStatus(
                id,
                request != null ? request.getStatus() : null,
                request != null ? request.getUsername() : null
        );
    }

    @PostMapping("/list")
    public MessageResponseDTO<List<SectionDto>> getAllSections() {
        return sectionService.getAllSections();
    }

    @PostMapping("/reference-data")
    public MessageResponseDTO<SectionReferenceDataDto> referenceData(
            @RequestBody(required = false) SectionReferenceDataRequest request) {
        return sectionService.getReferenceData(request);
    }

    @PostMapping("/filter-list")
    public MessageResponseDTO<SectionFilterResultDto> filterList(@RequestBody SectionFilterRequest request) {
        return sectionService.filterList(request);
    }

    @PostMapping("/{code}/deactivate")
    public MessageResponseDTO<String> deactivateSection(@PathVariable String code) {
        return sectionService.deactivateSection(code);
    }
}
