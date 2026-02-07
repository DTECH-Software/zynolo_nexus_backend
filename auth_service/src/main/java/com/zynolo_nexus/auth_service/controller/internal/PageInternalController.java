package com.zynolo_nexus.auth_service.controller.internal;

import com.zynolo_nexus.auth_service.service.PageManagementService;
import com.zynolo_nexus.contracts.pages.PageDto;
import com.zynolo_nexus.contracts.pages.PageRequest;
import com.zynolo_nexus.contracts.pages.PageStatusRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/internal/pages")
@RequiredArgsConstructor
public class PageInternalController {

    private final PageManagementService pageManagementService;

    @PostMapping
    public PageDto createPage(@RequestBody PageRequest request) {
        return pageManagementService.createPage(request);
    }

    @PostMapping("/{code}/update")
    public PageDto updatePage(@PathVariable String code, @RequestBody PageRequest request) {
        return pageManagementService.updatePage(code, request);
    }

    @PostMapping("/{code}/get")
    public PageDto getPage(@PathVariable String code) {
        return pageManagementService.getPage(code);
    }

    @PostMapping("/list")
    public List<PageDto> getAllPages(@RequestParam(required = false) String sectionCode) {
        return pageManagementService.getAllPages(sectionCode);
    }

    @PostMapping("/list-all")
    public List<PageDto> getAllPagesIncludingInactive(@RequestParam(required = false) String sectionCode) {
        return pageManagementService.getAllPagesIncludingInactive(sectionCode);
    }

    @PostMapping("/{code}/status")
    public PageDto updateStatus(@PathVariable String code, @RequestBody PageStatusRequest request) {
        return pageManagementService.updatePageStatus(code, request != null ? request.getStatus() : null);
    }

    @PostMapping("/{code}/deactivate")
    public void deactivatePage(@PathVariable String code) {
        pageManagementService.deactivatePage(code);
    }
}
