package com.zynolo_nexus.cheque_service.controller;

import com.zynolo_nexus.cheque_service.dto.api.MessageResponseDTO;
import com.zynolo_nexus.cheque_service.dto.request.ChequeSupplierCreateRequest;
import com.zynolo_nexus.cheque_service.dto.request.ChequeSupplierFilterRequest;
import com.zynolo_nexus.cheque_service.dto.request.ChequeSupplierReferenceDataRequest;
import com.zynolo_nexus.cheque_service.dto.request.ChequeSupplierStatusUpdateRequest;
import com.zynolo_nexus.cheque_service.dto.request.ChequeSupplierUpdateRequest;
import com.zynolo_nexus.cheque_service.dto.request.ChequeSupplierViewRequest;
import com.zynolo_nexus.cheque_service.dto.response.ChequeSupplierDto;
import com.zynolo_nexus.cheque_service.dto.response.ChequeSupplierFilterResultDto;
import com.zynolo_nexus.cheque_service.dto.response.ChequeSupplierReferenceDataDto;
import com.zynolo_nexus.cheque_service.service.ChequeSupplierService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/cheque/suppliers")
@RequiredArgsConstructor
public class ChequeSupplierController {

    private final ChequeSupplierService chequeSupplierService;

    @PostMapping
    public MessageResponseDTO<ChequeSupplierDto> create(@RequestBody ChequeSupplierCreateRequest request) {
        return chequeSupplierService.create(request);
    }

    @PostMapping("/view")
    public MessageResponseDTO<ChequeSupplierDto> view(@RequestBody ChequeSupplierViewRequest request) {
        Long id = request != null ? request.getId() : null;
        return chequeSupplierService.view(id);
    }

    @PostMapping("/update")
    public MessageResponseDTO<ChequeSupplierDto> update(@RequestBody ChequeSupplierUpdateRequest request) {
        return chequeSupplierService.update(request);
    }

    @PostMapping("/status")
    public MessageResponseDTO<ChequeSupplierDto> updateStatus(@RequestBody ChequeSupplierStatusUpdateRequest request) {
        return chequeSupplierService.updateStatus(request);
    }

    @PostMapping("/filter-list")
    public MessageResponseDTO<ChequeSupplierFilterResultDto> filterList(@RequestBody ChequeSupplierFilterRequest request) {
        return chequeSupplierService.filterList(request);
    }

    @PostMapping("/reference-data")
    public MessageResponseDTO<ChequeSupplierReferenceDataDto> referenceData(
            @RequestBody(required = false) ChequeSupplierReferenceDataRequest request) {
        return chequeSupplierService.referenceData(request);
    }
}
