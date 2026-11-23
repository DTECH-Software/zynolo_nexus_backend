package com.zynolo_nexus.po_service.controller;

import com.zynolo_nexus.po_service.dto.api.MessageResponseDTO;
import com.zynolo_nexus.po_service.dto.request.ApprovedPoRequestFilterRequest;
import com.zynolo_nexus.po_service.dto.request.PurchaseOrderCreateRequest;
import com.zynolo_nexus.po_service.dto.request.PurchaseOrderFilterRequest;
import com.zynolo_nexus.po_service.dto.request.PurchaseOrderReferenceDataRequest;
import com.zynolo_nexus.po_service.dto.request.PurchaseOrderRequestViewRequest;
import com.zynolo_nexus.po_service.dto.request.PurchaseOrderSendRequest;
import com.zynolo_nexus.po_service.dto.request.PurchaseOrderUpdateRequest;
import com.zynolo_nexus.po_service.dto.request.PurchaseOrderViewRequest;
import com.zynolo_nexus.po_service.dto.response.PoRequestDto;
import com.zynolo_nexus.po_service.dto.response.PoRequestFilterResultDto;
import com.zynolo_nexus.po_service.dto.response.PurchaseOrderDto;
import com.zynolo_nexus.po_service.dto.response.PurchaseOrderFilterResultDto;
import com.zynolo_nexus.po_service.dto.response.PurchaseOrderReferenceDataDto;
import com.zynolo_nexus.po_service.service.PurchaseOrderCreationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/po/po-creation")
public class PurchaseOrderCreationController {

    private final PurchaseOrderCreationService purchaseOrderCreationService;

    @PostMapping("/reference-data")
    public MessageResponseDTO<PurchaseOrderReferenceDataDto> referenceData(@Valid @RequestBody PurchaseOrderReferenceDataRequest request) {
        return success("Reference data POCR retrieved successfully", purchaseOrderCreationService.getReferenceData(request));
    }

    @PostMapping("/approved-requests")
    public MessageResponseDTO<PoRequestFilterResultDto> approvedRequests(@Valid @RequestBody ApprovedPoRequestFilterRequest request) {
        return success("Approved requests filtered successfully", purchaseOrderCreationService.approvedRequests(request));
    }

    @PostMapping("/request-view")
    public MessageResponseDTO<PoRequestDto> requestView(@Valid @RequestBody PurchaseOrderRequestViewRequest request) {
        return success("Approved PO request retrieved successfully", purchaseOrderCreationService.requestView(request));
    }

    @PostMapping
    public MessageResponseDTO<PurchaseOrderDto> create(@Valid @RequestBody PurchaseOrderCreateRequest request) {
        return success("Purchase order created successfully", purchaseOrderCreationService.create(request));
    }

    @PostMapping("/view")
    public MessageResponseDTO<PurchaseOrderDto> view(@Valid @RequestBody PurchaseOrderViewRequest request) {
        return success("Purchase order retrieved successfully", purchaseOrderCreationService.view(request));
    }

    @PostMapping("/update")
    public MessageResponseDTO<PurchaseOrderDto> update(@Valid @RequestBody PurchaseOrderUpdateRequest request) {
        return success("Purchase order updated successfully", purchaseOrderCreationService.update(request));
    }

    @PostMapping("/send-to-vendor")
    public MessageResponseDTO<PurchaseOrderDto> sendToVendor(@Valid @RequestBody PurchaseOrderSendRequest request) {
        return success("Purchase order sent to vendor successfully", purchaseOrderCreationService.sendToVendor(request));
    }

    @PostMapping("/filter-list")
    public MessageResponseDTO<PurchaseOrderFilterResultDto> filterList(@Valid @RequestBody PurchaseOrderFilterRequest request) {
        return success("Purchase orders filtered successfully", purchaseOrderCreationService.filterList(request));
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