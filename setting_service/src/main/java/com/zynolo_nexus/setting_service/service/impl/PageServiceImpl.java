package com.zynolo_nexus.setting_service.service.impl;

import com.zynolo_nexus.contracts.pages.PageDto;
import com.zynolo_nexus.contracts.pages.PageRequest;
import com.zynolo_nexus.setting_service.client.AuthModuleClient;
import com.zynolo_nexus.setting_service.dto.api.MessageResponseDTO;
import com.zynolo_nexus.setting_service.service.PageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PageServiceImpl implements PageService {

    private final AuthModuleClient authModuleClient;

    @Override
    public MessageResponseDTO<PageDto> createPage(PageRequest request) {
        PageDto page = authModuleClient.createPage(request);
        return MessageResponseDTO.<PageDto>builder()
                .success(true)
                .message("Page created successfully")
                .data(page)
                .errors(null)
                .errorCode(0)
                .responseTime(LocalDateTime.now())
                .build();
    }

    @Override
    public MessageResponseDTO<PageDto> updatePage(String code, PageRequest request) {
        PageDto page = authModuleClient.updatePage(code, request);
        return MessageResponseDTO.<PageDto>builder()
                .success(true)
                .message("Page updated successfully")
                .data(page)
                .errors(null)
                .errorCode(0)
                .responseTime(LocalDateTime.now())
                .build();
    }

    @Override
    public MessageResponseDTO<List<PageDto>> getAllPages() {
        List<PageDto> pages = authModuleClient.getAllPages();
        return MessageResponseDTO.<List<PageDto>>builder()
                .success(true)
                .message("Pages loaded successfully")
                .data(pages)
                .errors(null)
                .errorCode(0)
                .responseTime(LocalDateTime.now())
                .build();
    }

    @Override
    public MessageResponseDTO<String> deactivatePage(String code) {
        authModuleClient.deactivatePage(code);
        return MessageResponseDTO.<String>builder()
                .success(true)
                .message("Page deactivated successfully")
                .data(code)
                .errors(null)
                .errorCode(0)
                .responseTime(LocalDateTime.now())
                .build();
    }
}
