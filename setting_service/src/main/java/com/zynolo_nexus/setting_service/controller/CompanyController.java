package com.zynolo_nexus.setting_service.controller;

import com.zynolo_nexus.setting_service.dto.api.MessageResponseDTO;
import com.zynolo_nexus.setting_service.dto.request.CompanyCreateRequest;
import com.zynolo_nexus.setting_service.dto.request.CompanyFilterRequest;
import com.zynolo_nexus.setting_service.dto.request.CompanyReferenceDataRequest;
import com.zynolo_nexus.setting_service.dto.request.CompanyStatusUpdateRequest;
import com.zynolo_nexus.setting_service.dto.request.CompanyUpdateRequest;
import com.zynolo_nexus.setting_service.dto.request.CompanyViewRequest;
import com.zynolo_nexus.setting_service.dto.response.CompanyDto;
import com.zynolo_nexus.setting_service.dto.response.CompanyFilterResultDto;
import com.zynolo_nexus.setting_service.dto.response.CompanyReferenceDataDto;
import com.zynolo_nexus.setting_service.service.CompanyService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/setting/companies")
@RequiredArgsConstructor
public class CompanyController {

    private final CompanyService companyService;

    @PostMapping
    public MessageResponseDTO<CompanyDto> create(@RequestBody CompanyCreateRequest request) {
        return companyService.createCompany(request);
    }

    @PostMapping("/update")
    public MessageResponseDTO<CompanyDto> update(@RequestBody CompanyUpdateRequest request) {
        return companyService.updateCompany(request);
    }

    @PostMapping("/view")
    public MessageResponseDTO<CompanyDto> view(@RequestBody CompanyViewRequest request) {
        Long id = request != null ? request.getId() : null;
        return companyService.viewCompany(id);
    }

    @PostMapping("/status")
    public MessageResponseDTO<CompanyDto> status(@RequestBody CompanyStatusUpdateRequest request) {
        return companyService.updateStatus(request);
    }

    @PostMapping("/filter-list")
    public MessageResponseDTO<CompanyFilterResultDto> filterList(@RequestBody CompanyFilterRequest request) {
        return companyService.filterList(request);
    }

    @PostMapping("/reference-data")
    public MessageResponseDTO<CompanyReferenceDataDto> referenceData(
            @RequestBody(required = false) CompanyReferenceDataRequest request) {
        return companyService.getReferenceData(request);
    }
}
