package com.zynolo_nexus.cheque_service.controller;

import com.zynolo_nexus.cheque_service.dto.api.MessageResponseDTO;
import com.zynolo_nexus.cheque_service.dto.request.ChequeVoucherCreateRequest;
import com.zynolo_nexus.cheque_service.dto.request.ChequeVoucherExportPdfRequest;
import com.zynolo_nexus.cheque_service.dto.request.ChequeVoucherFilterRequest;
import com.zynolo_nexus.cheque_service.dto.request.ChequeVoucherReferenceDataRequest;
import com.zynolo_nexus.cheque_service.dto.request.ChequeVoucherUpdateRequest;
import com.zynolo_nexus.cheque_service.dto.request.ChequeVoucherViewRequest;
import com.zynolo_nexus.cheque_service.dto.response.ChequeVoucherDto;
import com.zynolo_nexus.cheque_service.dto.response.ChequeVoucherFilterResultDto;
import com.zynolo_nexus.cheque_service.dto.response.ChequeVoucherPdfDto;
import com.zynolo_nexus.cheque_service.dto.response.ChequeVoucherReferenceDataDto;
import com.zynolo_nexus.cheque_service.service.ChequeVoucherService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Base64;

@RestController
@RequestMapping("/api/v1/cheque/vouchers")
@RequiredArgsConstructor
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

    @PostMapping("/update")
    public MessageResponseDTO<ChequeVoucherDto> update(@RequestBody ChequeVoucherUpdateRequest request) {
        return chequeVoucherService.update(request);
    }

    @PostMapping("/filter-list")
    public MessageResponseDTO<ChequeVoucherFilterResultDto> filterList(@RequestBody ChequeVoucherFilterRequest request) {
        return chequeVoucherService.filterList(request);
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
    public ResponseEntity<byte[]> exportPdfDownload(@RequestBody ChequeVoucherExportPdfRequest request) {
        MessageResponseDTO<ChequeVoucherPdfDto> response = chequeVoucherService.exportPdf(request);
        if (response == null || !response.isSuccess() || response.getData() == null
                || !StringUtils.hasText(response.getData().getDoc())) {
            return ResponseEntity.badRequest().build();
        }

        byte[] pdfBytes = Base64.getDecoder().decode(response.getData().getDoc());
        String fileName = StringUtils.hasText(response.getData().getFileName())
                ? response.getData().getFileName()
                : "voucher.pdf";

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .contentLength(pdfBytes.length)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .body(pdfBytes);
    }
}
