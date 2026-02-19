package com.zynolo_nexus.cheque_service.controller;

import com.zynolo_nexus.cheque_service.dto.api.MessageResponseDTO;
import com.zynolo_nexus.cheque_service.dto.request.ChequeCustomerCreateRequest;
import com.zynolo_nexus.cheque_service.dto.request.ChequeCustomerFilterRequest;
import com.zynolo_nexus.cheque_service.dto.request.ChequeCustomerReferenceDataRequest;
import com.zynolo_nexus.cheque_service.dto.request.ChequeCustomerStatusUpdateRequest;
import com.zynolo_nexus.cheque_service.dto.request.ChequeCustomerUpdateRequest;
import com.zynolo_nexus.cheque_service.dto.request.ChequeCustomerViewRequest;
import com.zynolo_nexus.cheque_service.dto.response.ChequeCustomerDto;
import com.zynolo_nexus.cheque_service.dto.response.ChequeCustomerFilterResultDto;
import com.zynolo_nexus.cheque_service.dto.response.ChequeCustomerReferenceDataDto;
import com.zynolo_nexus.cheque_service.service.ChequeCustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/cheque/customers")
@RequiredArgsConstructor
public class ChequeCustomerController {

    private final ChequeCustomerService chequeCustomerService;

    @PostMapping
    public MessageResponseDTO<ChequeCustomerDto> create(@RequestBody ChequeCustomerCreateRequest request) {
        return chequeCustomerService.create(request);
    }

    @PostMapping("/view")
    public MessageResponseDTO<ChequeCustomerDto> view(@RequestBody ChequeCustomerViewRequest request) {
        Long id = request != null ? request.getId() : null;
        return chequeCustomerService.view(id);
    }

    @PostMapping("/update")
    public MessageResponseDTO<ChequeCustomerDto> update(@RequestBody ChequeCustomerUpdateRequest request) {
        return chequeCustomerService.update(request);
    }

    @PostMapping("/status")
    public MessageResponseDTO<ChequeCustomerDto> updateStatus(@RequestBody ChequeCustomerStatusUpdateRequest request) {
        return chequeCustomerService.updateStatus(request);
    }

    @PostMapping("/filter-list")
    public MessageResponseDTO<ChequeCustomerFilterResultDto> filterList(@RequestBody ChequeCustomerFilterRequest request) {
        return chequeCustomerService.filterList(request);
    }

    @PostMapping("/reference-data")
    public MessageResponseDTO<ChequeCustomerReferenceDataDto> referenceData(
            @RequestBody(required = false) ChequeCustomerReferenceDataRequest request) {
        return chequeCustomerService.referenceData(request);
    }
}
