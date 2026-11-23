package com.zynolo_nexus.po_service.controller;

import com.zynolo_nexus.po_service.dto.api.MessageResponseDTO;
import com.zynolo_nexus.po_service.dto.request.VendorCreateRequest;
import com.zynolo_nexus.po_service.dto.request.VendorFilterRequest;
import com.zynolo_nexus.po_service.dto.request.VendorReferenceDataRequest;
import com.zynolo_nexus.po_service.dto.request.VendorStatusRequest;
import com.zynolo_nexus.po_service.dto.request.VendorUpdateRequest;
import com.zynolo_nexus.po_service.dto.request.VendorViewRequest;
import com.zynolo_nexus.po_service.dto.response.VendorDto;
import com.zynolo_nexus.po_service.dto.response.VendorFilterResultDto;
import com.zynolo_nexus.po_service.dto.response.VendorReferenceDataDto;
import com.zynolo_nexus.po_service.service.VendorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/po/vendors")
public class VendorController {

    private final VendorService vendorService;

    @PostMapping("/reference-data")
    public MessageResponseDTO<VendorReferenceDataDto> referenceData(@Valid @RequestBody VendorReferenceDataRequest request) {
        return success("Reference data POVM retrieved successfully", vendorService.getReferenceData(request));
    }

    @PostMapping
    public MessageResponseDTO<VendorDto> create(@Valid @RequestBody VendorCreateRequest request) {
        return success("Vendor created successfully", vendorService.create(request));
    }

    @PostMapping("/view")
    public MessageResponseDTO<VendorDto> view(@Valid @RequestBody VendorViewRequest request) {
        return success("Vendor retrieved successfully", vendorService.view(request));
    }

    @PostMapping("/update")
    public MessageResponseDTO<VendorDto> update(@Valid @RequestBody VendorUpdateRequest request) {
        return success("Vendor updated successfully", vendorService.update(request));
    }

    @PostMapping("/status")
    public MessageResponseDTO<VendorDto> updateStatus(@Valid @RequestBody VendorStatusRequest request) {
        return success("Vendor status updated successfully", vendorService.updateStatus(request));
    }

    @PostMapping("/filter-list")
    public MessageResponseDTO<VendorFilterResultDto> filterList(@Valid @RequestBody VendorFilterRequest request) {
        return success("Vendors filtered successfully", vendorService.filterList(request));
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
