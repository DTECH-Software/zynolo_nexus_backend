package com.zynolo_nexus.setting_service.controller;

import com.zynolo_nexus.contracts.pages.SectionDto;
import com.zynolo_nexus.contracts.pages.SectionRequest;
import com.zynolo_nexus.setting_service.dto.api.MessageResponseDTO;
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

    @PutMapping("/{code}")
    public MessageResponseDTO<SectionDto> updateSection(@PathVariable String code,
                                                        @RequestBody SectionRequest request) {
        return sectionService.updateSection(code, request);
    }

    @GetMapping
    public MessageResponseDTO<List<SectionDto>> getAllSections() {
        return sectionService.getAllSections();
    }

    @DeleteMapping("/{code}")
    public MessageResponseDTO<String> deactivateSection(@PathVariable String code) {
        return sectionService.deactivateSection(code);
    }
}
