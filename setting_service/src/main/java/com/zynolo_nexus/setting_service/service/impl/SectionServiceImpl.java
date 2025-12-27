package com.zynolo_nexus.setting_service.service.impl;

import com.zynolo_nexus.contracts.pages.SectionDto;
import com.zynolo_nexus.contracts.pages.SectionRequest;
import com.zynolo_nexus.setting_service.client.AuthModuleClient;
import com.zynolo_nexus.setting_service.dto.api.MessageResponseDTO;
import com.zynolo_nexus.setting_service.service.SectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SectionServiceImpl implements SectionService {

    private final AuthModuleClient authModuleClient;

    @Override
    public MessageResponseDTO<SectionDto> createSection(SectionRequest request) {
        SectionDto section = authModuleClient.createSection(request);
        return MessageResponseDTO.<SectionDto>builder()
                .success(true)
                .message("Section created successfully")
                .data(section)
                .errors(null)
                .errorCode(0)
                .responseTime(LocalDateTime.now())
                .build();
    }

    @Override
    public MessageResponseDTO<SectionDto> updateSection(String code, SectionRequest request) {
        SectionDto section = authModuleClient.updateSection(code, request);
        return MessageResponseDTO.<SectionDto>builder()
                .success(true)
                .message("Section updated successfully")
                .data(section)
                .errors(null)
                .errorCode(0)
                .responseTime(LocalDateTime.now())
                .build();
    }

    @Override
    public MessageResponseDTO<List<SectionDto>> getAllSections() {
        List<SectionDto> sections = authModuleClient.getAllSections();
        return MessageResponseDTO.<List<SectionDto>>builder()
                .success(true)
                .message("Sections loaded successfully")
                .data(sections)
                .errors(null)
                .errorCode(0)
                .responseTime(LocalDateTime.now())
                .build();
    }

    @Override
    public MessageResponseDTO<String> deactivateSection(String code) {
        authModuleClient.deactivateSection(code);
        return MessageResponseDTO.<String>builder()
                .success(true)
                .message("Section deactivated successfully")
                .data(code)
                .errors(null)
                .errorCode(0)
                .responseTime(LocalDateTime.now())
                .build();
    }
}
