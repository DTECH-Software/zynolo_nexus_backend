package com.zynolo_nexus.setting_service.controller;

import com.zynolo_nexus.setting_service.dto.api.MessageResponseDTO;
import com.zynolo_nexus.setting_service.dto.request.UserCompanyCreateRequest;
import com.zynolo_nexus.setting_service.dto.request.UserCompanyFilterRequest;
import com.zynolo_nexus.setting_service.dto.request.UserCompanyReferenceDataRequest;
import com.zynolo_nexus.setting_service.dto.request.UserCompanyStatusUpdateRequest;
import com.zynolo_nexus.setting_service.dto.request.UserCompanyUpdateRequest;
import com.zynolo_nexus.setting_service.dto.request.UserCompanyViewRequest;
import com.zynolo_nexus.setting_service.dto.response.UserCompanyDto;
import com.zynolo_nexus.setting_service.dto.response.UserCompanyFilterResultDto;
import com.zynolo_nexus.setting_service.dto.response.UserCompanyReferenceDataDto;
import com.zynolo_nexus.setting_service.service.UserCompanyService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/setting/user-companies")
@RequiredArgsConstructor
public class UserCompanyController {

    private final UserCompanyService userCompanyService;

    @PostMapping
    public MessageResponseDTO<UserCompanyDto> create(@RequestBody UserCompanyCreateRequest request) {
        return userCompanyService.createUserCompany(request);
    }

    @PostMapping("/view")
    public MessageResponseDTO<UserCompanyDto> view(@RequestBody UserCompanyViewRequest request) {
        Long id = request != null ? request.getId() : null;
        return userCompanyService.viewUserCompany(id);
    }

    @PostMapping("/update")
    public MessageResponseDTO<UserCompanyDto> update(@RequestBody UserCompanyUpdateRequest request) {
        return userCompanyService.updateUserCompany(request);
    }

    @PostMapping("/status")
    public MessageResponseDTO<UserCompanyDto> status(@RequestBody UserCompanyStatusUpdateRequest request) {
        return userCompanyService.updateStatus(request);
    }

    @PostMapping("/filter-list")
    public MessageResponseDTO<UserCompanyFilterResultDto> filterList(@RequestBody UserCompanyFilterRequest request) {
        return userCompanyService.filterList(request);
    }

    @PostMapping("/reference-data")
    public MessageResponseDTO<UserCompanyReferenceDataDto> referenceData(
            @RequestBody(required = false) UserCompanyReferenceDataRequest request) {
        return userCompanyService.getReferenceData(request);
    }
}
