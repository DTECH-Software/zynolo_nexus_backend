package com.zynolo_nexus.cheque_service.controller;

import com.zynolo_nexus.cheque_service.dto.api.MessageResponseDTO;
import com.zynolo_nexus.cheque_service.dto.request.ChequeCompanyCreateRequest;
import com.zynolo_nexus.cheque_service.dto.request.ChequeCompanyFilterRequest;
import com.zynolo_nexus.cheque_service.dto.request.ChequeCompanyReferenceDataRequest;
import com.zynolo_nexus.cheque_service.dto.request.ChequeCompanyStatusUpdateRequest;
import com.zynolo_nexus.cheque_service.dto.request.ChequeCompanyUpdateRequest;
import com.zynolo_nexus.cheque_service.dto.request.ChequeCompanyViewRequest;
import com.zynolo_nexus.cheque_service.dto.response.ChequeCompanyDto;
import com.zynolo_nexus.cheque_service.dto.response.ChequeCompanyFilterResultDto;
import com.zynolo_nexus.cheque_service.dto.response.ChequeCompanyReferenceDataDto;
import com.zynolo_nexus.cheque_service.service.ChequeCompanyService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/cheque/companies")
@RequiredArgsConstructor
public class ChequeCompanyController {

    private final ChequeCompanyService chequeCompanyService;

    @PostMapping
    public MessageResponseDTO<ChequeCompanyDto> create(@RequestBody ChequeCompanyCreateRequest request) {
        return chequeCompanyService.create(request);
    }

    @PostMapping("/update")
    public MessageResponseDTO<ChequeCompanyDto> update(@RequestBody ChequeCompanyUpdateRequest request) {
        return chequeCompanyService.update(request);
    }

    @PostMapping("/view")
    public MessageResponseDTO<ChequeCompanyDto> view(@RequestBody ChequeCompanyViewRequest request) {
        Long id = request != null ? request.getId() : null;
        return chequeCompanyService.view(id);
    }

    @PostMapping("/status")
    public MessageResponseDTO<ChequeCompanyDto> status(@RequestBody ChequeCompanyStatusUpdateRequest request) {
        return chequeCompanyService.updateStatus(request);
    }

    @PostMapping("/filter-list")
    public MessageResponseDTO<ChequeCompanyFilterResultDto> filterList(@RequestBody ChequeCompanyFilterRequest request) {
        return chequeCompanyService.filterList(request);
    }

    @PostMapping("/reference-data")
    public MessageResponseDTO<ChequeCompanyReferenceDataDto> referenceData(
            @RequestBody(required = false) ChequeCompanyReferenceDataRequest request) {
        return chequeCompanyService.referenceData(request);
    }
}
