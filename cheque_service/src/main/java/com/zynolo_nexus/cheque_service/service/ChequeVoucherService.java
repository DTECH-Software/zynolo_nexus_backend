package com.zynolo_nexus.cheque_service.service;

import com.zynolo_nexus.cheque_service.dto.api.MessageResponseDTO;
import com.zynolo_nexus.cheque_service.dto.request.ChequePrintRequest;
import com.zynolo_nexus.cheque_service.dto.request.ChequeReprintApproveRequest;
import com.zynolo_nexus.cheque_service.dto.request.ChequeReprintCreateRequest;
import com.zynolo_nexus.cheque_service.dto.request.ChequeReprintFilterRequest;
import com.zynolo_nexus.cheque_service.dto.request.ChequeReprintReferenceDataRequest;
import com.zynolo_nexus.cheque_service.dto.request.ChequeReprintRejectRequest;
import com.zynolo_nexus.cheque_service.dto.request.ChequeVoucherApproveRequest;
import com.zynolo_nexus.cheque_service.dto.request.ChequeVoucherCreateRequest;
import com.zynolo_nexus.cheque_service.dto.request.ChequeVoucherExportPdfRequest;
import com.zynolo_nexus.cheque_service.dto.request.ChequeVoucherFilterRequest;
import com.zynolo_nexus.cheque_service.dto.request.ChequeVoucherReferenceDataRequest;
import com.zynolo_nexus.cheque_service.dto.request.ChequeVoucherRejectRequest;
import com.zynolo_nexus.cheque_service.dto.request.ChequeVoucherSubmitForApprovalRequest;
import com.zynolo_nexus.cheque_service.dto.request.ChequeVoucherUpdateRequest;
import com.zynolo_nexus.cheque_service.dto.response.ChequeReprintFilterResultDto;
import com.zynolo_nexus.cheque_service.dto.response.ChequeReprintReferenceDataDto;
import com.zynolo_nexus.cheque_service.dto.response.ChequeReprintRequestDto;
import com.zynolo_nexus.cheque_service.dto.response.ChequeVoucherDto;
import com.zynolo_nexus.cheque_service.dto.response.ChequeVoucherFilterResultDto;
import com.zynolo_nexus.cheque_service.dto.response.ChequeVoucherPdfDto;
import com.zynolo_nexus.cheque_service.dto.response.ChequeVoucherReferenceDataDto;

public interface ChequeVoucherService {

    MessageResponseDTO<ChequeVoucherDto> create(ChequeVoucherCreateRequest request);

    MessageResponseDTO<ChequeVoucherDto> view(Long id);

    MessageResponseDTO<ChequeVoucherDto> update(ChequeVoucherUpdateRequest request);

    MessageResponseDTO<ChequeVoucherFilterResultDto> filterList(ChequeVoucherFilterRequest request);

    MessageResponseDTO<ChequeVoucherFilterResultDto> approvalFilterList(ChequeVoucherFilterRequest request);

    MessageResponseDTO<ChequeVoucherFilterResultDto> chequeFilterList(ChequeVoucherFilterRequest request);

    MessageResponseDTO<ChequeVoucherDto> chequeView(Long id);

    MessageResponseDTO<ChequeVoucherDto> submitForApproval(ChequeVoucherSubmitForApprovalRequest request);

    MessageResponseDTO<ChequeVoucherDto> approve(ChequeVoucherApproveRequest request);

    MessageResponseDTO<ChequeVoucherDto> reject(ChequeVoucherRejectRequest request);

    MessageResponseDTO<ChequeVoucherReferenceDataDto> referenceData(ChequeVoucherReferenceDataRequest request);

    MessageResponseDTO<ChequeVoucherPdfDto> exportPdf(ChequeVoucherExportPdfRequest request);

    MessageResponseDTO<ChequeVoucherPdfDto> previewCheque(ChequePrintRequest request);

    MessageResponseDTO<ChequeVoucherPdfDto> printCheque(ChequePrintRequest request);

    MessageResponseDTO<ChequeReprintRequestDto> requestReprint(ChequeReprintCreateRequest request);

    MessageResponseDTO<ChequeReprintReferenceDataDto> reprintReferenceData(ChequeReprintReferenceDataRequest request);

    MessageResponseDTO<ChequeReprintFilterResultDto> reprintFilterList(ChequeReprintFilterRequest request);

    MessageResponseDTO<ChequeReprintRequestDto> reprintView(Long id);

    MessageResponseDTO<ChequeReprintRequestDto> approveReprint(ChequeReprintApproveRequest request);

    MessageResponseDTO<ChequeReprintRequestDto> rejectReprint(ChequeReprintRejectRequest request);
}
