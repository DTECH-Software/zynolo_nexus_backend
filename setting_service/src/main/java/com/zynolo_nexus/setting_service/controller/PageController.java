package com.zynolo_nexus.setting_service.controller;

import com.zynolo_nexus.contracts.pages.PageDto;
import com.zynolo_nexus.contracts.pages.PageRequest;
import com.zynolo_nexus.contracts.pages.PageStatusRequest;
import com.zynolo_nexus.setting_service.dto.api.MessageResponseDTO;
import com.zynolo_nexus.setting_service.dto.request.PageFilterRequest;
import com.zynolo_nexus.setting_service.dto.request.PageUpdateRequest;
import com.zynolo_nexus.setting_service.dto.response.PageFilterResultDto;
import com.zynolo_nexus.setting_service.dto.response.PageReferenceDataDto;
import com.zynolo_nexus.setting_service.service.PageService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/setting/pages")
@RequiredArgsConstructor
public class PageController {

    private final PageService pageService;

    @PostMapping
    public MessageResponseDTO<PageDto> createPage(@RequestBody PageRequest request) {
        return pageService.createPage(request);
    }

    @PostMapping("/{code}/update")
    public MessageResponseDTO<PageDto> updatePage(@PathVariable String code,
                                                  @RequestBody PageUpdateRequest request) {
        return pageService.updatePage(code, request);
    }

    @PostMapping("/{code}/get")
    public MessageResponseDTO<PageDto> getPage(@PathVariable String code) {
        return pageService.getPage(code);
    }

    @PostMapping("/{code}/status")
    public MessageResponseDTO<PageDto> updateStatus(@PathVariable String code,
                                                    @RequestBody PageStatusRequest request) {
        return pageService.updatePageStatus(code, request != null ? request.getStatus() : null);
    }

    @PostMapping("/list")
    public MessageResponseDTO<List<PageDto>> getAllPages() {
        return pageService.getAllPages();
    }

    @PostMapping("/reference-data")
    public MessageResponseDTO<PageReferenceDataDto> referenceData() {
        return pageService.getReferenceData();
    }

    @PostMapping("/filter-list")
    public MessageResponseDTO<PageFilterResultDto> filterList(@RequestBody PageFilterRequest request) {
        return pageService.filterList(request);
    }

    @PostMapping("/{code}/deactivate")
    public MessageResponseDTO<String> deactivatePage(@PathVariable String code) {
        return pageService.deactivatePage(code);
    }
}
