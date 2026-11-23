package com.zynolo_nexus.cheque_service.controller;

import com.zynolo_nexus.cheque_service.dto.api.MessageResponseDTO;
import com.zynolo_nexus.cheque_service.dto.request.ChequePrintRequest;
import com.zynolo_nexus.cheque_service.dto.request.ChequeReprintCreateRequest;
import com.zynolo_nexus.cheque_service.dto.request.ChequeVoucherFilterRequest;
import com.zynolo_nexus.cheque_service.dto.request.ChequeVoucherReferenceDataRequest;
import com.zynolo_nexus.cheque_service.dto.request.ChequeVoucherViewRequest;
import com.zynolo_nexus.cheque_service.dto.response.ChequeReprintRequestDto;
import com.zynolo_nexus.cheque_service.dto.response.ChequeVoucherDto;
import com.zynolo_nexus.cheque_service.dto.response.ChequeVoucherFilterResultDto;
import com.zynolo_nexus.cheque_service.dto.response.ChequeVoucherPdfDto;
import com.zynolo_nexus.cheque_service.dto.response.ChequeVoucherReferenceDataDto;
import com.zynolo_nexus.cheque_service.service.ChequeVoucherService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Base64;

@RestController
@RequestMapping("/api/v1/cheque/cheques")
@RequiredArgsConstructor
public class ChequeCreationController {

    private static final String PAGE_CODE = "CHCP";

    private final ChequeVoucherService chequeVoucherService;

    @PostMapping("/reference-data")
    public MessageResponseDTO<ChequeVoucherReferenceDataDto> referenceData(
            @RequestBody(required = false) ChequeVoucherReferenceDataRequest request) {
        ChequeVoucherReferenceDataRequest payload = request != null ? request : new ChequeVoucherReferenceDataRequest();
        if (!StringUtils.hasText(payload.getPageCode())) {
            payload.setPageCode(PAGE_CODE);
        }
        return chequeVoucherService.referenceData(payload);
    }

    @PostMapping("/filter-list")
    public MessageResponseDTO<ChequeVoucherFilterResultDto> filterList(@RequestBody ChequeVoucherFilterRequest request) {
        return chequeVoucherService.chequeFilterList(request);
    }

    @PostMapping("/view")
    public MessageResponseDTO<ChequeVoucherDto> view(@RequestBody ChequeVoucherViewRequest request) {
        Long id = request != null ? request.getId() : null;
        return chequeVoucherService.chequeView(id);
    }

    @PostMapping("/print")
    public MessageResponseDTO<ChequeVoucherPdfDto> print(@RequestBody ChequePrintRequest request) {
        return chequeVoucherService.previewCheque(request);
    }

    @PostMapping("/print-download")
    public ResponseEntity<?> printDownload(@RequestBody ChequePrintRequest request) {
        MessageResponseDTO<ChequeVoucherPdfDto> response = chequeVoucherService.printCheque(request);
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
            MessageResponseDTO<ChequeVoucherPdfDto> error = MessageResponseDTO.<ChequeVoucherPdfDto>builder()
                    .success(false)
                    .message("Unable to decode cheque PDF")
                    .data(null)
                    .errors(ex.getMessage())
                    .errorCode(500)
                    .responseTime(LocalDateTime.now())
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }

        String fileName = StringUtils.hasText(response.getData().getFileName())
                ? response.getData().getFileName()
                : "cheque.pdf";

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .contentLength(pdfBytes.length)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .body(pdfBytes);
    }

    @PostMapping("/request-reprint")
    public MessageResponseDTO<ChequeReprintRequestDto> requestReprint(@RequestBody ChequeReprintCreateRequest request) {
        return chequeVoucherService.requestReprint(request);
    }
}
