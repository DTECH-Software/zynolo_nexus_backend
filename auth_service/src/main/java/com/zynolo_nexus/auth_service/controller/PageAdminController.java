package com.zynolo_nexus.auth_service.controller;

import com.zynolo_nexus.auth_service.dto.api.MessageResponseDTO;
import com.zynolo_nexus.auth_service.service.PageManagementService;
import com.zynolo_nexus.contracts.pages.PageDto;
import com.zynolo_nexus.contracts.pages.PageRequest;
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
@RequestMapping("/api/v1/pages")
@RequiredArgsConstructor
public class PageAdminController {

    private final PageManagementService pageManagementService;

    @PostMapping
    public MessageResponseDTO<PageDto> create(@RequestBody PageRequest request) {
        PageDto page = pageManagementService.createPage(request);
        return wrap(page, "page.create.success");
    }

    @PutMapping("/{code}")
    public MessageResponseDTO<PageDto> update(@PathVariable String code, @RequestBody PageRequest request) {
        PageDto page = pageManagementService.updatePage(code, request);
        return wrap(page, "page.update.success");
    }

    @GetMapping
    public MessageResponseDTO<List<PageDto>> getAll(@RequestParam(required = false) String sectionCode) {
        List<PageDto> pages = pageManagementService.getAllPages(sectionCode);
        return MessageResponseDTO.<List<PageDto>>builder()
                .success(true)
                .message("page.fetch.success")
                .data(pages)
                .errors(null)
                .errorCode(0)
                .responseTime(LocalDateTime.now())
                .build();
    }

    @DeleteMapping("/{code}")
    public MessageResponseDTO<String> deactivate(@PathVariable String code) {
        pageManagementService.deactivatePage(code);
        return MessageResponseDTO.<String>builder()
                .success(true)
                .message("page.deactivate.success")
                .data("deactivated")
                .errors(null)
                .errorCode(0)
                .responseTime(LocalDateTime.now())
                .build();
    }

    private MessageResponseDTO<PageDto> wrap(PageDto dto, String message) {
        return MessageResponseDTO.<PageDto>builder()
                .success(true)
                .message(message)
                .data(dto)
                .errors(null)
                .errorCode(0)
                .responseTime(LocalDateTime.now())
                .build();
    }
}
