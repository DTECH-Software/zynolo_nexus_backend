package com.zynolo_nexus.po_service.controller;

import com.zynolo_nexus.po_service.dto.api.MessageResponseDTO;
import com.zynolo_nexus.po_service.dto.request.PoReferenceDataRequest;
import com.zynolo_nexus.po_service.dto.request.PoApprovalVendorProductsRequest;
import com.zynolo_nexus.po_service.dto.request.PoRequestApproveRequest;
import com.zynolo_nexus.po_service.dto.request.PoRequestFilterRequest;
import com.zynolo_nexus.po_service.dto.request.PoRequestRejectRequest;
import com.zynolo_nexus.po_service.dto.request.PoRequestViewRequest;
import com.zynolo_nexus.po_service.dto.response.PoRequestDto;
import com.zynolo_nexus.po_service.dto.response.PoRequestFilterResultDto;
import com.zynolo_nexus.po_service.dto.response.PoRequestReferenceDataDto;
import com.zynolo_nexus.po_service.dto.response.PoApprovalVendorProductDto;
import com.zynolo_nexus.po_service.service.PoRequestApprovalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/po/request-approval")
public class PoRequestApprovalController {

    private final PoRequestApprovalService poRequestApprovalService;

    @PostMapping("/reference-data")
    public MessageResponseDTO<PoRequestReferenceDataDto> referenceData(@Valid @RequestBody PoReferenceDataRequest request) {
        return success("Reference data PORA retrieved successfully", poRequestApprovalService.getReferenceData(request));
    }

    @PostMapping("/filter-list")
    public MessageResponseDTO<PoRequestFilterResultDto> filterList(@Valid @RequestBody PoRequestFilterRequest request) {
        return success("PO requests filtered successfully", poRequestApprovalService.filterList(request));
    }

    @PostMapping("/view")
    public MessageResponseDTO<PoRequestDto> view(@Valid @RequestBody PoRequestViewRequest request) {
        return success("PO request retrieved successfully", poRequestApprovalService.view(request));
    }

    @PostMapping("/vendor-products")
    public MessageResponseDTO<List<PoApprovalVendorProductDto>> vendorProducts(
            @Valid @RequestBody PoApprovalVendorProductsRequest request) {
        return success("Vendor products checked successfully", poRequestApprovalService.getVendorProducts(request));
    }

    @PostMapping("/approve")
    public MessageResponseDTO<PoRequestDto> approve(@Valid @RequestBody PoRequestApproveRequest request) {
        return success("PO request approved successfully", poRequestApprovalService.approve(request));
    }

    @PostMapping("/reject")
    public MessageResponseDTO<PoRequestDto> reject(@Valid @RequestBody PoRequestRejectRequest request) {
        return success("PO request rejected successfully", poRequestApprovalService.reject(request));
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
