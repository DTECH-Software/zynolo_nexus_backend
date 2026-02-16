package com.zynolo_nexus.setting_service.controller;

import com.zynolo_nexus.setting_service.dto.api.MessageResponseDTO;
import com.zynolo_nexus.setting_service.dto.request.CompanyModuleCheckRequest;
import com.zynolo_nexus.setting_service.dto.request.CompanyModuleCreateRequest;
import com.zynolo_nexus.setting_service.dto.request.CompanyModuleFilterRequest;
import com.zynolo_nexus.setting_service.dto.request.CompanyModuleReferenceDataRequest;
import com.zynolo_nexus.setting_service.dto.request.CompanyModuleStatusUpdateRequest;
import com.zynolo_nexus.setting_service.dto.request.CompanyModuleUpdateRequest;
import com.zynolo_nexus.setting_service.dto.request.CompanyModuleViewRequest;
import com.zynolo_nexus.setting_service.dto.response.CompanyModuleCheckDto;
import com.zynolo_nexus.setting_service.dto.response.CompanyModuleDto;
import com.zynolo_nexus.setting_service.dto.response.CompanyModuleFilterResultDto;
import com.zynolo_nexus.setting_service.dto.response.CompanyModuleReferenceDataDto;
import com.zynolo_nexus.setting_service.service.CompanyModuleSubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/setting/company-modules")
@RequiredArgsConstructor
public class CompanyModuleSubscriptionController {

    private final CompanyModuleSubscriptionService companyModuleSubscriptionService;

    @PostMapping
    public MessageResponseDTO<CompanyModuleDto> create(@RequestBody CompanyModuleCreateRequest request) {
        return companyModuleSubscriptionService.create(request);
    }

    @PostMapping("/view")
    public MessageResponseDTO<CompanyModuleDto> view(@RequestBody CompanyModuleViewRequest request) {
        return companyModuleSubscriptionService.view(request != null ? request.getId() : null);
    }

    @PostMapping("/update")
    public MessageResponseDTO<CompanyModuleDto> update(@RequestBody CompanyModuleUpdateRequest request) {
        return companyModuleSubscriptionService.update(request);
    }

    @PostMapping("/status")
    public MessageResponseDTO<CompanyModuleDto> status(@RequestBody CompanyModuleStatusUpdateRequest request) {
        return companyModuleSubscriptionService.updateStatus(request);
    }

    @PostMapping("/filter-list")
    public MessageResponseDTO<CompanyModuleFilterResultDto> filterList(@RequestBody CompanyModuleFilterRequest request) {
        return companyModuleSubscriptionService.filterList(request);
    }

    @PostMapping("/reference-data")
    public MessageResponseDTO<CompanyModuleReferenceDataDto> referenceData(
            @RequestBody(required = false) CompanyModuleReferenceDataRequest request) {
        return companyModuleSubscriptionService.referenceData(request);
    }

    @PostMapping("/check")
    public MessageResponseDTO<CompanyModuleCheckDto> check(@RequestBody CompanyModuleCheckRequest request) {
        return companyModuleSubscriptionService.check(request);
    }
}
