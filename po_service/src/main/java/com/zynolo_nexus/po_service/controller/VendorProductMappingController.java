package com.zynolo_nexus.po_service.controller;

import com.zynolo_nexus.po_service.dto.api.MessageResponseDTO;
import com.zynolo_nexus.po_service.dto.request.VendorProductCreateRequest;
import com.zynolo_nexus.po_service.dto.request.VendorProductFilterRequest;
import com.zynolo_nexus.po_service.dto.request.VendorProductReferenceDataRequest;
import com.zynolo_nexus.po_service.dto.request.VendorProductStatusRequest;
import com.zynolo_nexus.po_service.dto.request.VendorProductUpdateRequest;
import com.zynolo_nexus.po_service.dto.request.VendorProductViewRequest;
import com.zynolo_nexus.po_service.dto.response.VendorProductDto;
import com.zynolo_nexus.po_service.dto.response.VendorProductFilterResultDto;
import com.zynolo_nexus.po_service.dto.response.VendorProductReferenceDataDto;
import com.zynolo_nexus.po_service.service.VendorProductMappingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/po/vendor-products")
public class VendorProductMappingController {

    private final VendorProductMappingService vendorProductMappingService;

    @PostMapping("/reference-data")
    public MessageResponseDTO<VendorProductReferenceDataDto> referenceData(@Valid @RequestBody VendorProductReferenceDataRequest request) {
        return success("Reference data VPMP retrieved successfully", vendorProductMappingService.getReferenceData(request));
    }

    @PostMapping
    public MessageResponseDTO<VendorProductDto> create(@Valid @RequestBody VendorProductCreateRequest request) {
        return success("Vendor product mapping created successfully", vendorProductMappingService.create(request));
    }

    @PostMapping("/view")
    public MessageResponseDTO<VendorProductDto> view(@Valid @RequestBody VendorProductViewRequest request) {
        return success("Vendor product mapping retrieved successfully", vendorProductMappingService.view(request));
    }

    @PostMapping("/update")
    public MessageResponseDTO<VendorProductDto> update(@Valid @RequestBody VendorProductUpdateRequest request) {
        return success("Vendor product mapping updated successfully", vendorProductMappingService.update(request));
    }

    @PostMapping("/status")
    public MessageResponseDTO<VendorProductDto> updateStatus(@Valid @RequestBody VendorProductStatusRequest request) {
        return success("Vendor product mapping status updated successfully", vendorProductMappingService.updateStatus(request));
    }

    @PostMapping("/filter-list")
    public MessageResponseDTO<VendorProductFilterResultDto> filterList(@Valid @RequestBody VendorProductFilterRequest request) {
        return success("Vendor product mappings filtered successfully", vendorProductMappingService.filterList(request));
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
