package com.zynolo_nexus.po_service.controller;

import com.zynolo_nexus.po_service.dto.api.MessageResponseDTO;
import com.zynolo_nexus.po_service.dto.request.PoReferenceDataRequest;
import com.zynolo_nexus.po_service.dto.request.PoRequestFilterRequest;
import com.zynolo_nexus.po_service.dto.request.PoRequestViewRequest;
import com.zynolo_nexus.po_service.dto.response.PoRequestDto;
import com.zynolo_nexus.po_service.dto.response.PoRequestFilterResultDto;
import com.zynolo_nexus.po_service.dto.response.PoRequestReferenceDataDto;
import com.zynolo_nexus.po_service.service.PoRequestManagementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/po/request-management")
public class PoRequestManagementController {

    private final PoRequestManagementService poRequestManagementService;

    @PostMapping("/reference-data")
    public MessageResponseDTO<PoRequestReferenceDataDto> referenceData(@Valid @RequestBody PoReferenceDataRequest request) {
        return success("Reference data PORM retrieved successfully", poRequestManagementService.getReferenceData(request));
    }

    @PostMapping("/view")
    public MessageResponseDTO<PoRequestDto> view(@Valid @RequestBody PoRequestViewRequest request) {
        return success("PO request retrieved successfully", poRequestManagementService.view(request));
    }

    @PostMapping("/filter-list")
    public MessageResponseDTO<PoRequestFilterResultDto> filterList(@Valid @RequestBody PoRequestFilterRequest request) {
        return success("PO requests filtered successfully", poRequestManagementService.filterList(request));
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
