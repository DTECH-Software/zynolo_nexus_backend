package com.zynolo_nexus.auth_service.controller;

import com.zynolo_nexus.auth_service.dto.api.MessageResponseDTO;
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

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/sections")
@RequiredArgsConstructor
public class SectionAdminController {

    private final SectionManagementService sectionManagementService;

    @PostMapping
    public MessageResponseDTO<SectionDto> create(@RequestBody SectionRequest request) {
        SectionDto section = sectionManagementService.createSection(request);
        return wrap(section, "section.create.success");
    }

    @PutMapping("/{code}")
    public MessageResponseDTO<SectionDto> update(@PathVariable String code, @RequestBody SectionRequest request) {
        SectionDto section = sectionManagementService.updateSection(code, request);
        return wrap(section, "section.update.success");
    }

    @GetMapping
    public MessageResponseDTO<List<SectionDto>> getAll(@RequestParam(required = false) String moduleCode) {
        List<SectionDto> sections = sectionManagementService.getAllSections(moduleCode);
        return MessageResponseDTO.<List<SectionDto>>builder()
                .success(true)
                .message("section.fetch.success")
                .data(sections)
                .errors(null)
                .errorCode(0)
                .responseTime(LocalDateTime.now())
                .build();
    }

    @DeleteMapping("/{code}")
    public MessageResponseDTO<String> deactivate(@PathVariable String code) {
        sectionManagementService.deactivateSection(code);
        return MessageResponseDTO.<String>builder()
                .success(true)
                .message("section.deactivate.success")
                .data("deactivated")
                .errors(null)
                .errorCode(0)
                .responseTime(LocalDateTime.now())
                .build();
    }

    private MessageResponseDTO<SectionDto> wrap(SectionDto dto, String message) {
        return MessageResponseDTO.<SectionDto>builder()
                .success(true)
                .message(message)
                .data(dto)
                .errors(null)
                .errorCode(0)
                .responseTime(LocalDateTime.now())
                .build();
    }
}
