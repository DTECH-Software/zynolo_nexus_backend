package com.zynolo_nexus.cheque_service.controller;

import com.zynolo_nexus.cheque_service.dto.api.MessageResponseDTO;
import com.zynolo_nexus.cheque_service.dto.request.ChequeVoucherCreateRequest;
import com.zynolo_nexus.cheque_service.dto.request.ChequeVoucherReferenceDataRequest;
import com.zynolo_nexus.cheque_service.dto.response.ChequeVoucherDto;
import com.zynolo_nexus.cheque_service.dto.response.ChequeVoucherReferenceDataDto;
import com.zynolo_nexus.cheque_service.service.ChequeVoucherService;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/cheque/voucher-create")
@RequiredArgsConstructor
public class ChequeVoucherCreateController {

    private static final String PAGE_CODE = "CHVC";

    private final ChequeVoucherService chequeVoucherService;

    @PostMapping
    public MessageResponseDTO<ChequeVoucherDto> create(@RequestBody ChequeVoucherCreateRequest request) {
        return chequeVoucherService.create(request);
    }

    @PostMapping("/reference-data")
    public MessageResponseDTO<ChequeVoucherReferenceDataDto> referenceData(
            @RequestBody(required = false) ChequeVoucherReferenceDataRequest request) {
        ChequeVoucherReferenceDataRequest payload = request != null ? request : new ChequeVoucherReferenceDataRequest();
        if (!StringUtils.hasText(payload.getPageCode())) {
            payload.setPageCode(PAGE_CODE);
        }
        return chequeVoucherService.referenceData(payload);
    }
}
