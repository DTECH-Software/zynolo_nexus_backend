package com.zynolo_nexus.cheque_service.controller;

import com.zynolo_nexus.cheque_service.dto.api.MessageResponseDTO;
import com.zynolo_nexus.cheque_service.dto.request.ChequeVoucherApproveRequest;
import com.zynolo_nexus.cheque_service.dto.request.ChequeVoucherCreateRequest;
import com.zynolo_nexus.cheque_service.dto.request.ChequeVoucherExportPdfRequest;
import com.zynolo_nexus.cheque_service.dto.request.ChequeVoucherFilterRequest;
import com.zynolo_nexus.cheque_service.dto.request.ChequeVoucherRejectRequest;
import com.zynolo_nexus.cheque_service.dto.request.ChequeVoucherReferenceDataRequest;
import com.zynolo_nexus.cheque_service.dto.request.ChequeVoucherSubmitForApprovalRequest;
import com.zynolo_nexus.cheque_service.dto.request.ChequeVoucherUpdateRequest;
import com.zynolo_nexus.cheque_service.dto.request.ChequeVoucherViewRequest;
import com.zynolo_nexus.cheque_service.dto.response.ChequeVoucherDto;
import com.zynolo_nexus.cheque_service.dto.response.ChequeVoucherFilterResultDto;
import com.zynolo_nexus.cheque_service.dto.response.ChequeVoucherPdfDto;
import com.zynolo_nexus.cheque_service.dto.response.ChequeVoucherReferenceDataDto;
import com.zynolo_nexus.cheque_service.service.ChequeVoucherService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Base64;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/cheque/vouchers")
@RequiredArgsConstructor
@Slf4j
public class ChequeVoucherController {

    private final ChequeVoucherService chequeVoucherService;

    @PostMapping
    public MessageResponseDTO<ChequeVoucherDto> create(@RequestBody ChequeVoucherCreateRequest request) {
        return chequeVoucherService.create(request);
    }

    @PostMapping("/view")
    public MessageResponseDTO<ChequeVoucherDto> view(@RequestBody ChequeVoucherViewRequest request) {
        Long id = request != null ? request.getId() : null;
        return chequeVoucherService.view(id);
    }

    @PostMapping("/approval/view")
    public MessageResponseDTO<ChequeVoucherDto> approvalView(@RequestBody ChequeVoucherViewRequest request) {
        Long id = request != null ? request.getId() : null;
        return chequeVoucherService.view(id);
    }

    @PostMapping("/update")
    public MessageResponseDTO<ChequeVoucherDto> update(@RequestBody ChequeVoucherUpdateRequest request) {
        return chequeVoucherService.update(request);
    }

    @PostMapping("/filter-list")
    public MessageResponseDTO<ChequeVoucherFilterResultDto> filterList(@RequestBody ChequeVoucherFilterRequest request) {
        return chequeVoucherService.filterList(request);
    }

    @PostMapping("/approval/filter-list")
    public MessageResponseDTO<ChequeVoucherFilterResultDto> approvalFilterList(
            @RequestBody ChequeVoucherFilterRequest request) {
        return chequeVoucherService.approvalFilterList(request);
    }

    @PostMapping("/submit-for-approval")
    public MessageResponseDTO<ChequeVoucherDto> submitForApproval(
            @RequestBody ChequeVoucherSubmitForApprovalRequest request) {
        return chequeVoucherService.submitForApproval(request);
    }

    @PostMapping("/approve")
    public MessageResponseDTO<ChequeVoucherDto> approve(@RequestBody ChequeVoucherApproveRequest request) {
        return chequeVoucherService.approve(request);
    }

    @PostMapping("/reject")
    public MessageResponseDTO<ChequeVoucherDto> reject(@RequestBody ChequeVoucherRejectRequest request) {
        return chequeVoucherService.reject(request);
    }

    @PostMapping("/reference-data")
    public MessageResponseDTO<ChequeVoucherReferenceDataDto> referenceData(
            @RequestBody(required = false) ChequeVoucherReferenceDataRequest request) {
        return chequeVoucherService.referenceData(request);
    }

    @PostMapping("/export-pdf")
    public MessageResponseDTO<ChequeVoucherPdfDto> exportPdf(@RequestBody ChequeVoucherExportPdfRequest request) {
        return chequeVoucherService.exportPdf(request);
    }

    @PostMapping("/export-pdf-download")
    public ResponseEntity<?> exportPdfDownload(@RequestBody ChequeVoucherExportPdfRequest request) {
        try {
            normalizeExportRequest(request);
            MessageResponseDTO<ChequeVoucherPdfDto> response = chequeVoucherService.exportPdf(request);
            if (response == null) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
            if (!response.isSuccess() || response.getData() == null || !StringUtils.hasText(response.getData().getDoc())) {
                int code = response.getErrorCode() > 0 ? response.getErrorCode() : 400;
                HttpStatus status = HttpStatus.resolve(code);
                return ResponseEntity.status(status != null ? status : HttpStatus.BAD_REQUEST).body(response);
            }

            byte[] pdfBytes;
            try {
                pdfBytes = Base64.getDecoder().decode(response.getData().getDoc());
            } catch (IllegalArgumentException ex) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }
            String fileName = StringUtils.hasText(response.getData().getFileName())
                    ? response.getData().getFileName()
                    : "voucher.pdf";

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .contentLength(pdfBytes.length)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                    .body(pdfBytes);
        } catch (Throwable ex) {
            log.error("Unhandled error in export-pdf-download", ex);
            MessageResponseDTO<ChequeVoucherPdfDto> error = MessageResponseDTO.<ChequeVoucherPdfDto>builder()
                    .success(false)
                    .message("Unable to export voucher PDF")
                    .data(null)
                    .errors(ex.getMessage())
                    .errorCode(500)
                    .responseTime(LocalDateTime.now())
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    private void normalizeExportRequest(ChequeVoucherExportPdfRequest request) {
        if (request == null) {
            return;
        }
        if (request.getVoucherId() == null && request.getId() != null) {
            request.setVoucherId(request.getId());
        }
    }
}
