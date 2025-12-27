package com.zynolo_nexus.setting_service.controller;

import com.zynolo_nexus.contracts.pages.PageDto;
import com.zynolo_nexus.contracts.pages.PageRequest;
import com.zynolo_nexus.setting_service.dto.api.MessageResponseDTO;
import com.zynolo_nexus.setting_service.service.PageService;
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
@RequestMapping("/api/v1/setting/pages")
@RequiredArgsConstructor
public class PageController {

    private final PageService pageService;

    @PostMapping
    public MessageResponseDTO<PageDto> createPage(@RequestBody PageRequest request) {
        return pageService.createPage(request);
    }

    @PutMapping("/{code}")
    public MessageResponseDTO<PageDto> updatePage(@PathVariable String code,
                                                  @RequestBody PageRequest request) {
        return pageService.updatePage(code, request);
    }

    @GetMapping
    public MessageResponseDTO<List<PageDto>> getAllPages() {
        return pageService.getAllPages();
    }

    @DeleteMapping("/{code}")
    public MessageResponseDTO<String> deactivatePage(@PathVariable String code) {
        return pageService.deactivatePage(code);
    }
}
