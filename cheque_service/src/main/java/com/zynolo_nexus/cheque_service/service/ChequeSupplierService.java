package com.zynolo_nexus.cheque_service.service;

import com.zynolo_nexus.cheque_service.dto.api.MessageResponseDTO;
import com.zynolo_nexus.cheque_service.dto.request.ChequeSupplierCreateRequest;
import com.zynolo_nexus.cheque_service.dto.request.ChequeSupplierFilterRequest;
import com.zynolo_nexus.cheque_service.dto.request.ChequeSupplierReferenceDataRequest;
import com.zynolo_nexus.cheque_service.dto.request.ChequeSupplierStatusUpdateRequest;
import com.zynolo_nexus.cheque_service.dto.request.ChequeSupplierUpdateRequest;
import com.zynolo_nexus.cheque_service.dto.response.ChequeSupplierDto;
import com.zynolo_nexus.cheque_service.dto.response.ChequeSupplierFilterResultDto;
import com.zynolo_nexus.cheque_service.dto.response.ChequeSupplierReferenceDataDto;

public interface ChequeSupplierService {

    MessageResponseDTO<ChequeSupplierDto> create(ChequeSupplierCreateRequest request);

    MessageResponseDTO<ChequeSupplierDto> update(ChequeSupplierUpdateRequest request);

    MessageResponseDTO<ChequeSupplierDto> view(Long id);

    MessageResponseDTO<ChequeSupplierDto> updateStatus(ChequeSupplierStatusUpdateRequest request);

    MessageResponseDTO<ChequeSupplierFilterResultDto> filterList(ChequeSupplierFilterRequest request);

    MessageResponseDTO<ChequeSupplierReferenceDataDto> referenceData(ChequeSupplierReferenceDataRequest request);
}
