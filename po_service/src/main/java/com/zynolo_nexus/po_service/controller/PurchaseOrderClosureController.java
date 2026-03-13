package com.zynolo_nexus.po_service.controller;

import com.zynolo_nexus.po_service.dto.api.MessageResponseDTO;
import com.zynolo_nexus.po_service.dto.request.ClosureCloseRequest;
import com.zynolo_nexus.po_service.dto.request.ClosureFilterRequest;
import com.zynolo_nexus.po_service.dto.request.ClosureReferenceDataRequest;
import com.zynolo_nexus.po_service.dto.request.ClosureViewRequest;
import com.zynolo_nexus.po_service.dto.response.ClosureFilterResultDto;
import com.zynolo_nexus.po_service.dto.response.ClosureReferenceDataDto;
import com.zynolo_nexus.po_service.dto.response.ClosureViewDto;
import com.zynolo_nexus.po_service.service.PurchaseOrderClosureService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/po/closure")
public class PurchaseOrderClosureController {

    private final PurchaseOrderClosureService purchaseOrderClosureService;

    @PostMapping("/reference-data")
    public MessageResponseDTO<ClosureReferenceDataDto> referenceData(@Valid @RequestBody ClosureReferenceDataRequest request) {
        return success("Reference data POCL retrieved successfully", purchaseOrderClosureService.getReferenceData(request));
    }

    @PostMapping("/filter-list")
    public MessageResponseDTO<ClosureFilterResultDto> filterList(@Valid @RequestBody ClosureFilterRequest request) {
        return success("Purchase orders filtered successfully", purchaseOrderClosureService.filterList(request));
    }

    @PostMapping("/view")
    public MessageResponseDTO<ClosureViewDto> view(@Valid @RequestBody ClosureViewRequest request) {
        return success("Purchase order closure details retrieved successfully", purchaseOrderClosureService.view(request));
    }

    @PostMapping("/close")
    public MessageResponseDTO<ClosureViewDto> close(@Valid @RequestBody ClosureCloseRequest request) {
        return success("Purchase order closed successfully", purchaseOrderClosureService.close(request));
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
