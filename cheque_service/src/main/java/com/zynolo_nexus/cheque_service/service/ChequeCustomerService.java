package com.zynolo_nexus.cheque_service.service;

import com.zynolo_nexus.cheque_service.dto.api.MessageResponseDTO;
import com.zynolo_nexus.cheque_service.dto.request.ChequeCustomerCreateRequest;
import com.zynolo_nexus.cheque_service.dto.request.ChequeCustomerFilterRequest;
import com.zynolo_nexus.cheque_service.dto.request.ChequeCustomerReferenceDataRequest;
import com.zynolo_nexus.cheque_service.dto.request.ChequeCustomerStatusUpdateRequest;
import com.zynolo_nexus.cheque_service.dto.request.ChequeCustomerUpdateRequest;
import com.zynolo_nexus.cheque_service.dto.response.ChequeCustomerDto;
import com.zynolo_nexus.cheque_service.dto.response.ChequeCustomerFilterResultDto;
import com.zynolo_nexus.cheque_service.dto.response.ChequeCustomerReferenceDataDto;

public interface ChequeCustomerService {

    MessageResponseDTO<ChequeCustomerDto> create(ChequeCustomerCreateRequest request);

    MessageResponseDTO<ChequeCustomerDto> update(ChequeCustomerUpdateRequest request);

    MessageResponseDTO<ChequeCustomerDto> view(Long id);

    MessageResponseDTO<ChequeCustomerDto> updateStatus(ChequeCustomerStatusUpdateRequest request);

    MessageResponseDTO<ChequeCustomerFilterResultDto> filterList(ChequeCustomerFilterRequest request);

    MessageResponseDTO<ChequeCustomerReferenceDataDto> referenceData(ChequeCustomerReferenceDataRequest request);
}
