package com.zynolo_nexus.po_service.controller;

import com.zynolo_nexus.po_service.dto.api.MessageResponseDTO;
import com.zynolo_nexus.po_service.dto.request.PoReferenceDataRequest;
import com.zynolo_nexus.po_service.dto.request.PoRequestCreateRequest;
import com.zynolo_nexus.po_service.dto.request.PoRequestFilterRequest;
import com.zynolo_nexus.po_service.dto.request.PoRequestSubmitRequest;
import com.zynolo_nexus.po_service.dto.request.PoRequestUpdateRequest;
import com.zynolo_nexus.po_service.dto.request.PoRequestViewRequest;
import com.zynolo_nexus.po_service.dto.response.PoRequestDto;
import com.zynolo_nexus.po_service.dto.response.PoRequestFilterResultDto;
import com.zynolo_nexus.po_service.dto.response.PoRequestReferenceDataDto;
import com.zynolo_nexus.po_service.service.PoRequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/po/request-create")
public class PoRequestCreateController {

    private final PoRequestService poRequestService;

    @PostMapping("/reference-data")
    public MessageResponseDTO<PoRequestReferenceDataDto> referenceData(@Valid @RequestBody PoReferenceDataRequest request) {
        return success("Reference data PORC retrieved successfully", poRequestService.getReferenceData(request));
    }

    @PostMapping
    public MessageResponseDTO<PoRequestDto> create(@Valid @RequestBody PoRequestCreateRequest request) {
        return success("PO request created successfully", poRequestService.create(request));
    }

    @PostMapping("/view")
    public MessageResponseDTO<PoRequestDto> view(@Valid @RequestBody PoRequestViewRequest request) {
        return success("PO request retrieved successfully", poRequestService.view(request));
    }

    @PostMapping("/update")
    public MessageResponseDTO<PoRequestDto> update(@Valid @RequestBody PoRequestUpdateRequest request) {
        return success("PO request updated successfully", poRequestService.update(request));
    }

    @PostMapping("/submit")
    public MessageResponseDTO<PoRequestDto> submit(@Valid @RequestBody PoRequestSubmitRequest request) {
        return success("PO request submitted successfully", poRequestService.submit(request));
    }

    @PostMapping("/filter-list")
    public MessageResponseDTO<PoRequestFilterResultDto> filterList(@Valid @RequestBody PoRequestFilterRequest request) {
        return success("PO requests filtered successfully", poRequestService.filterList(request));
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
