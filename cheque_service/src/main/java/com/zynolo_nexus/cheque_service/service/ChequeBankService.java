package com.zynolo_nexus.cheque_service.service;

import com.zynolo_nexus.cheque_service.dto.api.MessageResponseDTO;
import com.zynolo_nexus.cheque_service.dto.request.ChequeBankCreateRequest;
import com.zynolo_nexus.cheque_service.dto.request.ChequeBankFilterRequest;
import com.zynolo_nexus.cheque_service.dto.request.ChequeBankReferenceDataRequest;
import com.zynolo_nexus.cheque_service.dto.request.ChequeBankStatusUpdateRequest;
import com.zynolo_nexus.cheque_service.dto.request.ChequeBankUpdateRequest;
import com.zynolo_nexus.cheque_service.dto.response.ChequeBankDto;
import com.zynolo_nexus.cheque_service.dto.response.ChequeBankFilterResultDto;
import com.zynolo_nexus.cheque_service.dto.response.ChequeBankReferenceDataDto;

public interface ChequeBankService {

    MessageResponseDTO<ChequeBankDto> create(ChequeBankCreateRequest request);

    MessageResponseDTO<ChequeBankDto> update(ChequeBankUpdateRequest request);

    MessageResponseDTO<ChequeBankDto> view(Long id);

    MessageResponseDTO<ChequeBankDto> updateStatus(ChequeBankStatusUpdateRequest request);

    MessageResponseDTO<ChequeBankFilterResultDto> filterList(ChequeBankFilterRequest request);

    MessageResponseDTO<ChequeBankReferenceDataDto> referenceData(ChequeBankReferenceDataRequest request);
}
