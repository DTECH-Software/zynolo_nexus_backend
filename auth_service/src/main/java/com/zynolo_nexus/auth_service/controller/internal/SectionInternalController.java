package com.zynolo_nexus.auth_service.controller.internal;

import com.zynolo_nexus.auth_service.service.SectionManagementService;
import com.zynolo_nexus.contracts.pages.SectionDto;
import com.zynolo_nexus.contracts.pages.SectionRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/internal/sections")
@RequiredArgsConstructor
public class SectionInternalController {

    private final SectionManagementService sectionManagementService;

    @PostMapping
    public SectionDto createSection(@RequestBody SectionRequest request) {
        return sectionManagementService.createSection(request);
    }

    @PutMapping("/{code}")
    public SectionDto updateSection(@PathVariable String code, @RequestBody SectionRequest request) {
        return sectionManagementService.updateSection(code, request);
    }

    @GetMapping
    public List<SectionDto> getAllSections(@RequestParam(required = false) String moduleCode) {
        return sectionManagementService.getAllSections(moduleCode);
    }

    @DeleteMapping("/{code}")
    public void deactivateSection(@PathVariable String code) {
        sectionManagementService.deactivateSection(code);
    }
}
