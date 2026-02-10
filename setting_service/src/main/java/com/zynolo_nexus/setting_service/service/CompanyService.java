package com.zynolo_nexus.setting_service.service;

import com.zynolo_nexus.setting_service.dto.api.MessageResponseDTO;
import com.zynolo_nexus.setting_service.dto.request.CompanyCreateRequest;
import com.zynolo_nexus.setting_service.dto.request.CompanyFilterRequest;
import com.zynolo_nexus.setting_service.dto.request.CompanyReferenceDataRequest;
import com.zynolo_nexus.setting_service.dto.request.CompanyStatusUpdateRequest;
import com.zynolo_nexus.setting_service.dto.request.CompanyUpdateRequest;
import com.zynolo_nexus.setting_service.dto.response.CompanyDto;
import com.zynolo_nexus.setting_service.dto.response.CompanyFilterResultDto;
import com.zynolo_nexus.setting_service.dto.response.CompanyReferenceDataDto;

public interface CompanyService {

    MessageResponseDTO<CompanyDto> createCompany(CompanyCreateRequest request);

    MessageResponseDTO<CompanyDto> updateCompany(CompanyUpdateRequest request);

    MessageResponseDTO<CompanyDto> viewCompany(Long id);

    MessageResponseDTO<CompanyDto> updateStatus(CompanyStatusUpdateRequest request);

    MessageResponseDTO<CompanyFilterResultDto> filterList(CompanyFilterRequest request);

    MessageResponseDTO<CompanyReferenceDataDto> getReferenceData(CompanyReferenceDataRequest request);
}
