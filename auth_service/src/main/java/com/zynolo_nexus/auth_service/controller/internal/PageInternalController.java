package com.zynolo_nexus.auth_service.controller.internal;

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

    @PutMapping("/{code}")
    public PageDto updatePage(@PathVariable String code, @RequestBody PageRequest request) {
        return pageManagementService.updatePage(code, request);
    }

    @GetMapping
    public List<PageDto> getAllPages(@RequestParam(required = false) String sectionCode) {
        return pageManagementService.getAllPages(sectionCode);
    }

    @DeleteMapping("/{code}")
    public void deactivatePage(@PathVariable String code) {
        pageManagementService.deactivatePage(code);
    }
}
