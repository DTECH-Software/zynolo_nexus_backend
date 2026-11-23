package com.zynolo_nexus.setting_service.service;

import com.zynolo_nexus.setting_service.dto.api.MessageResponseDTO;
import com.zynolo_nexus.setting_service.dto.request.BankCreateRequest;
import com.zynolo_nexus.setting_service.dto.request.BankFilterRequest;
import com.zynolo_nexus.setting_service.dto.request.BankReferenceDataRequest;
import com.zynolo_nexus.setting_service.dto.request.BankStatusUpdateRequest;
import com.zynolo_nexus.setting_service.dto.request.BankUpdateRequest;
import com.zynolo_nexus.setting_service.dto.response.BankDto;
import com.zynolo_nexus.setting_service.dto.response.BankFilterResultDto;
import com.zynolo_nexus.setting_service.dto.response.BankReferenceDataDto;

public interface BankService {

    MessageResponseDTO<BankDto> create(BankCreateRequest request);

    MessageResponseDTO<BankDto> update(BankUpdateRequest request);

    MessageResponseDTO<BankDto> view(Long id);

    MessageResponseDTO<BankDto> updateStatus(BankStatusUpdateRequest request);

    MessageResponseDTO<BankFilterResultDto> filterList(BankFilterRequest request);

    MessageResponseDTO<BankReferenceDataDto> referenceData(BankReferenceDataRequest request);
}
