package com.zynolo_nexus.cheque_service.service;

import com.zynolo_nexus.cheque_service.dto.api.MessageResponseDTO;
import com.zynolo_nexus.cheque_service.dto.request.ChequeVoucherCreateRequest;
import com.zynolo_nexus.cheque_service.dto.request.ChequeVoucherExportPdfRequest;
import com.zynolo_nexus.cheque_service.dto.request.ChequeVoucherFilterRequest;
import com.zynolo_nexus.cheque_service.dto.request.ChequeVoucherReferenceDataRequest;
import com.zynolo_nexus.cheque_service.dto.request.ChequeVoucherUpdateRequest;
import com.zynolo_nexus.cheque_service.dto.response.ChequeVoucherDto;
import com.zynolo_nexus.cheque_service.dto.response.ChequeVoucherFilterResultDto;
import com.zynolo_nexus.cheque_service.dto.response.ChequeVoucherPdfDto;
import com.zynolo_nexus.cheque_service.dto.response.ChequeVoucherReferenceDataDto;

public interface ChequeVoucherService {

    MessageResponseDTO<ChequeVoucherDto> create(ChequeVoucherCreateRequest request);

    MessageResponseDTO<ChequeVoucherDto> view(Long id);

    MessageResponseDTO<ChequeVoucherDto> update(ChequeVoucherUpdateRequest request);

    MessageResponseDTO<ChequeVoucherFilterResultDto> filterList(ChequeVoucherFilterRequest request);

    MessageResponseDTO<ChequeVoucherReferenceDataDto> referenceData(ChequeVoucherReferenceDataRequest request);

    MessageResponseDTO<ChequeVoucherPdfDto> exportPdf(ChequeVoucherExportPdfRequest request);
}
