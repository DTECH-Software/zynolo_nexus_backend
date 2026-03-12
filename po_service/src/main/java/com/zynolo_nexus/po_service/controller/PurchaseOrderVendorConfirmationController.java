package com.zynolo_nexus.po_service.controller;

import com.zynolo_nexus.po_service.dto.api.MessageResponseDTO;
import com.zynolo_nexus.po_service.dto.request.PurchaseOrderFilterRequest;
import com.zynolo_nexus.po_service.dto.request.PurchaseOrderReferenceDataRequest;
import com.zynolo_nexus.po_service.dto.request.PurchaseOrderVendorConfirmRequest;
import com.zynolo_nexus.po_service.dto.request.PurchaseOrderViewRequest;
import com.zynolo_nexus.po_service.dto.response.PurchaseOrderDto;
import com.zynolo_nexus.po_service.dto.response.PurchaseOrderFilterResultDto;
import com.zynolo_nexus.po_service.dto.response.PurchaseOrderReferenceDataDto;
import com.zynolo_nexus.po_service.service.PurchaseOrderVendorConfirmationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/po/po-vendor-confirmation")
public class PurchaseOrderVendorConfirmationController {

    private final PurchaseOrderVendorConfirmationService purchaseOrderVendorConfirmationService;

    @PostMapping("/reference-data")
    public MessageResponseDTO<PurchaseOrderReferenceDataDto> referenceData(@Valid @RequestBody PurchaseOrderReferenceDataRequest request) {
        return success("Reference data POVC retrieved successfully", purchaseOrderVendorConfirmationService.getReferenceData(request));
    }

    @PostMapping("/filter-list")
    public MessageResponseDTO<PurchaseOrderFilterResultDto> filterList(@Valid @RequestBody PurchaseOrderFilterRequest request) {
        return success("Purchase orders filtered successfully", purchaseOrderVendorConfirmationService.filterList(request));
    }

    @PostMapping("/view")
    public MessageResponseDTO<PurchaseOrderDto> view(@Valid @RequestBody PurchaseOrderViewRequest request) {
        return success("Purchase order retrieved successfully", purchaseOrderVendorConfirmationService.view(request));
    }

    @PostMapping("/confirm")
    public MessageResponseDTO<PurchaseOrderDto> confirm(@Valid @RequestBody PurchaseOrderVendorConfirmRequest request) {
        return success("Vendor confirmation updated successfully", purchaseOrderVendorConfirmationService.confirm(request));
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
