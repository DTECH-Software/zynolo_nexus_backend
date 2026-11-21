package com.zynolo_nexus.fleet_service.controller;

import com.zynolo_nexus.fleet_service.dto.api.MessageResponseDTO;
import com.zynolo_nexus.fleet_service.dto.request.ReceiptFilterRequest;
import com.zynolo_nexus.fleet_service.dto.request.ReceiptReferenceDataRequest;
import com.zynolo_nexus.fleet_service.dto.request.ReceiptViewRequest;
import com.zynolo_nexus.fleet_service.dto.response.ReceiptDto;
import com.zynolo_nexus.fleet_service.dto.response.ReceiptFilterResultDto;
import com.zynolo_nexus.fleet_service.dto.response.ReceiptReferenceDataDto;
import com.zynolo_nexus.fleet_service.service.ReceiptService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/fleet/receipts")
@Tag(name = "FLRC - Receipts", description = "Page code FLRC (`FLEET_PAYMENT`) — list/view fare receipts")
public class ReceiptController {

    private final ReceiptService receiptService;

    @PostMapping("/reference-data")
    public MessageResponseDTO<ReceiptReferenceDataDto> referenceData(
            @Valid @RequestBody ReceiptReferenceDataRequest request
    ) {
        return success("Reference data FLRC retrieved successfully", receiptService.getReferenceData(request));
    }

    @PostMapping("/filter-list")
    public MessageResponseDTO<ReceiptFilterResultDto> filterList(
            @Valid @RequestBody ReceiptFilterRequest request
    ) {
        return success("Receipts filtered successfully", receiptService.filterList(request));
    }

    @PostMapping("/view")
    public MessageResponseDTO<ReceiptDto> view(@Valid @RequestBody ReceiptViewRequest request) {
        return success("Receipt retrieved successfully", receiptService.view(request));
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
