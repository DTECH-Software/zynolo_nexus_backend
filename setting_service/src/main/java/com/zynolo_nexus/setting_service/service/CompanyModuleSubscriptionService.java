package com.zynolo_nexus.setting_service.service;

import com.zynolo_nexus.setting_service.dto.api.MessageResponseDTO;
import com.zynolo_nexus.setting_service.dto.request.CompanyModuleBulkUpdateRequest;
import com.zynolo_nexus.setting_service.dto.request.CompanyModuleBulkViewRequest;
import com.zynolo_nexus.setting_service.dto.request.CompanyModuleCheckRequest;
import com.zynolo_nexus.setting_service.dto.request.CompanyModuleCreateRequest;
import com.zynolo_nexus.setting_service.dto.request.CompanyModuleFilterRequest;
import com.zynolo_nexus.setting_service.dto.request.CompanyModuleReferenceDataRequest;
import com.zynolo_nexus.setting_service.dto.request.CompanyModuleStatusUpdateRequest;
import com.zynolo_nexus.setting_service.dto.request.CompanyModuleUpdateRequest;
import com.zynolo_nexus.setting_service.dto.response.CompanyModuleBulkViewDto;
import com.zynolo_nexus.setting_service.dto.response.CompanyModuleCheckDto;
import com.zynolo_nexus.setting_service.dto.response.CompanyModuleDto;
import com.zynolo_nexus.setting_service.dto.response.CompanyModuleFilterResultDto;
import com.zynolo_nexus.setting_service.dto.response.CompanyModuleReferenceDataDto;

public interface CompanyModuleSubscriptionService {

    MessageResponseDTO<CompanyModuleDto> create(CompanyModuleCreateRequest request);

    MessageResponseDTO<CompanyModuleDto> view(Long id);

    MessageResponseDTO<CompanyModuleDto> update(CompanyModuleUpdateRequest request);

    MessageResponseDTO<CompanyModuleDto> updateStatus(CompanyModuleStatusUpdateRequest request);

    MessageResponseDTO<CompanyModuleFilterResultDto> filterList(CompanyModuleFilterRequest request);

    MessageResponseDTO<CompanyModuleReferenceDataDto> referenceData(CompanyModuleReferenceDataRequest request);

    MessageResponseDTO<CompanyModuleCheckDto> check(CompanyModuleCheckRequest request);

    MessageResponseDTO<CompanyModuleBulkViewDto> bulkView(CompanyModuleBulkViewRequest request);

    MessageResponseDTO<CompanyModuleBulkViewDto> bulkUpdate(CompanyModuleBulkUpdateRequest request);
}
