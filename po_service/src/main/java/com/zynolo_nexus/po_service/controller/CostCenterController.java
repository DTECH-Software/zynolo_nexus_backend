package com.zynolo_nexus.po_service.controller;

import com.zynolo_nexus.po_service.dto.api.MessageResponseDTO;
import com.zynolo_nexus.po_service.dto.request.CostCenterCreateRequest;
import com.zynolo_nexus.po_service.dto.request.CostCenterFilterRequest;
import com.zynolo_nexus.po_service.dto.request.CostCenterReferenceDataRequest;
import com.zynolo_nexus.po_service.dto.request.CostCenterStatusRequest;
import com.zynolo_nexus.po_service.dto.request.CostCenterUpdateRequest;
import com.zynolo_nexus.po_service.dto.request.CostCenterViewRequest;
import com.zynolo_nexus.po_service.dto.response.CostCenterDto;
import com.zynolo_nexus.po_service.dto.response.CostCenterFilterResultDto;
import com.zynolo_nexus.po_service.dto.response.CostCenterReferenceDataDto;
import com.zynolo_nexus.po_service.service.CostCenterService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/po/cost-centers")
public class CostCenterController {

    private final CostCenterService costCenterService;

    @PostMapping("/reference-data")
    public MessageResponseDTO<CostCenterReferenceDataDto> referenceData(@Valid @RequestBody CostCenterReferenceDataRequest request) {
        return success("Reference data CCEM retrieved successfully", costCenterService.getReferenceData(request));
    }

    @PostMapping
    public MessageResponseDTO<CostCenterDto> create(@Valid @RequestBody CostCenterCreateRequest request) {
        return success("Cost center created successfully", costCenterService.create(request));
    }

    @PostMapping("/view")
    public MessageResponseDTO<CostCenterDto> view(@Valid @RequestBody CostCenterViewRequest request) {
        return success("Cost center retrieved successfully", costCenterService.view(request));
    }

    @PostMapping("/update")
    public MessageResponseDTO<CostCenterDto> update(@Valid @RequestBody CostCenterUpdateRequest request) {
        return success("Cost center updated successfully", costCenterService.update(request));
    }

    @PostMapping("/status")
    public MessageResponseDTO<CostCenterDto> updateStatus(@Valid @RequestBody CostCenterStatusRequest request) {
        return success("Cost center status updated successfully", costCenterService.updateStatus(request));
    }

    @PostMapping("/filter-list")
    public MessageResponseDTO<CostCenterFilterResultDto> filterList(@Valid @RequestBody CostCenterFilterRequest request) {
        return success("Cost centers filtered successfully", costCenterService.filterList(request));
    }

    private <T> MessageResponseDTO<T> success(String message, T data) {
        return MessageResponseDTO.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .errors(null)
                .errorCode(0)
                .responseTime(LocalDateTime.now())
                .build();
    }
}
