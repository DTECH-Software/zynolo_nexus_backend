package com.zynolo_nexus.po_service.controller;

import com.zynolo_nexus.po_service.dto.api.MessageResponseDTO;
import com.zynolo_nexus.po_service.dto.request.TrackingExportRequest;
import com.zynolo_nexus.po_service.dto.request.TrackingFilterRequest;
import com.zynolo_nexus.po_service.dto.request.TrackingReferenceDataRequest;
import com.zynolo_nexus.po_service.dto.request.TrackingTimelineRequest;
import com.zynolo_nexus.po_service.dto.request.TrackingViewRequest;
import com.zynolo_nexus.po_service.dto.response.TrackingExportDto;
import com.zynolo_nexus.po_service.dto.response.TrackingFilterResultDto;
import com.zynolo_nexus.po_service.dto.response.TrackingReferenceDataDto;
import com.zynolo_nexus.po_service.dto.response.TrackingTimelineDto;
import com.zynolo_nexus.po_service.dto.response.TrackingViewDto;
import com.zynolo_nexus.po_service.service.PurchaseOrderTrackingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/po/tracking")
public class PurchaseOrderTrackingController {

    private final PurchaseOrderTrackingService purchaseOrderTrackingService;

    @PostMapping("/reference-data")
    public MessageResponseDTO<TrackingReferenceDataDto> referenceData(@Valid @RequestBody TrackingReferenceDataRequest request) {
        return success("Reference data POTR retrieved successfully", purchaseOrderTrackingService.getReferenceData(request));
    }

    @PostMapping("/filter-list")
    public MessageResponseDTO<TrackingFilterResultDto> filterList(@Valid @RequestBody TrackingFilterRequest request) {
        return success("PO tracking records filtered successfully", purchaseOrderTrackingService.filterList(request));
    }

    @PostMapping("/view")
    public MessageResponseDTO<TrackingViewDto> view(@Valid @RequestBody TrackingViewRequest request) {
        return success("PO tracking details retrieved successfully", purchaseOrderTrackingService.view(request));
    }

    @PostMapping("/timeline")
    public MessageResponseDTO<TrackingTimelineDto> timeline(@Valid @RequestBody TrackingTimelineRequest request) {
        return success("PO tracking timeline retrieved successfully", purchaseOrderTrackingService.timeline(request));
    }

    @PostMapping("/export")
    public MessageResponseDTO<TrackingExportDto> export(@Valid @RequestBody TrackingExportRequest request) {
        return success("PO tracking export retrieved successfully", purchaseOrderTrackingService.export(request));
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
