package com.zynolo_nexus.cheque_service.service;

import com.zynolo_nexus.cheque_service.dto.api.MessageResponseDTO;
import com.zynolo_nexus.cheque_service.dto.request.ChequeCompanyCreateRequest;
import com.zynolo_nexus.cheque_service.dto.request.ChequeCompanyFilterRequest;
import com.zynolo_nexus.cheque_service.dto.request.ChequeCompanyReferenceDataRequest;
import com.zynolo_nexus.cheque_service.dto.request.ChequeCompanyStatusUpdateRequest;
import com.zynolo_nexus.cheque_service.dto.request.ChequeCompanyUpdateRequest;
import com.zynolo_nexus.cheque_service.dto.response.ChequeCompanyDto;
import com.zynolo_nexus.cheque_service.dto.response.ChequeCompanyFilterResultDto;
import com.zynolo_nexus.cheque_service.dto.response.ChequeCompanyReferenceDataDto;

public interface ChequeCompanyService {

    MessageResponseDTO<ChequeCompanyDto> create(ChequeCompanyCreateRequest request);

    MessageResponseDTO<ChequeCompanyDto> update(ChequeCompanyUpdateRequest request);

    MessageResponseDTO<ChequeCompanyDto> view(Long id);

    MessageResponseDTO<ChequeCompanyDto> updateStatus(ChequeCompanyStatusUpdateRequest request);

    MessageResponseDTO<ChequeCompanyFilterResultDto> filterList(ChequeCompanyFilterRequest request);

    MessageResponseDTO<ChequeCompanyReferenceDataDto> referenceData(ChequeCompanyReferenceDataRequest request);
}
