package com.zynolo_nexus.setting_service.service;

import com.zynolo_nexus.setting_service.dto.api.MessageResponseDTO;
import com.zynolo_nexus.setting_service.dto.request.UserCompanyCreateRequest;
import com.zynolo_nexus.setting_service.dto.request.UserCompanyFilterRequest;
import com.zynolo_nexus.setting_service.dto.request.UserCompanyReferenceDataRequest;
import com.zynolo_nexus.setting_service.dto.request.UserCompanyStatusUpdateRequest;
import com.zynolo_nexus.setting_service.dto.request.UserCompanyUpdateRequest;
import com.zynolo_nexus.setting_service.dto.response.UserCompanyDto;
import com.zynolo_nexus.setting_service.dto.response.UserCompanyFilterResultDto;
import com.zynolo_nexus.setting_service.dto.response.UserCompanyReferenceDataDto;

public interface UserCompanyService {

    MessageResponseDTO<UserCompanyDto> createUserCompany(UserCompanyCreateRequest request);

    MessageResponseDTO<UserCompanyDto> updateUserCompany(UserCompanyUpdateRequest request);

    MessageResponseDTO<UserCompanyDto> viewUserCompany(Long id);

    MessageResponseDTO<UserCompanyDto> updateStatus(UserCompanyStatusUpdateRequest request);

    MessageResponseDTO<UserCompanyFilterResultDto> filterList(UserCompanyFilterRequest request);

    MessageResponseDTO<UserCompanyReferenceDataDto> getReferenceData(UserCompanyReferenceDataRequest request);
}
