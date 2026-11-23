package com.zynolo_nexus.cheque_service.controller;

import com.zynolo_nexus.cheque_service.dto.api.MessageResponseDTO;
import com.zynolo_nexus.cheque_service.dto.request.ChequeReprintApproveRequest;
import com.zynolo_nexus.cheque_service.dto.request.ChequeReprintFilterRequest;
import com.zynolo_nexus.cheque_service.dto.request.ChequeReprintReferenceDataRequest;
import com.zynolo_nexus.cheque_service.dto.request.ChequeReprintRejectRequest;
import com.zynolo_nexus.cheque_service.dto.request.ChequeReprintViewRequest;
import com.zynolo_nexus.cheque_service.dto.response.ChequeReprintFilterResultDto;
import com.zynolo_nexus.cheque_service.dto.response.ChequeReprintReferenceDataDto;
import com.zynolo_nexus.cheque_service.dto.response.ChequeReprintRequestDto;
import com.zynolo_nexus.cheque_service.service.ChequeVoucherService;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/cheque/cheque-approvals")
@RequiredArgsConstructor
public class ChequeReprintApprovalController {

    private static final String PAGE_CODE = "CHAP";

    private final ChequeVoucherService chequeVoucherService;

    @PostMapping("/reference-data")
    public MessageResponseDTO<ChequeReprintReferenceDataDto> referenceData(
            @RequestBody(required = false) ChequeReprintReferenceDataRequest request) {
        ChequeReprintReferenceDataRequest payload = request != null ? request : new ChequeReprintReferenceDataRequest();
        if (!StringUtils.hasText(payload.getPageCode())) {
            payload.setPageCode(PAGE_CODE);
        }
        return chequeVoucherService.reprintReferenceData(payload);
    }

    @PostMapping("/filter-list")
    public MessageResponseDTO<ChequeReprintFilterResultDto> filterList(@RequestBody ChequeReprintFilterRequest request) {
        return chequeVoucherService.reprintFilterList(request);
    }

    @PostMapping("/view")
    public MessageResponseDTO<ChequeReprintRequestDto> view(@RequestBody ChequeReprintViewRequest request) {
        Long id = request != null ? request.getId() : null;
        return chequeVoucherService.reprintView(id);
    }

    @PostMapping("/approve")
    public MessageResponseDTO<ChequeReprintRequestDto> approve(@RequestBody ChequeReprintApproveRequest request) {
        return chequeVoucherService.approveReprint(request);
    }

    @PostMapping("/reject")
    public MessageResponseDTO<ChequeReprintRequestDto> reject(@RequestBody ChequeReprintRejectRequest request) {
        return chequeVoucherService.rejectReprint(request);
    }
}
