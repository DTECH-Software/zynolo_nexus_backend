package com.zynolo_nexus.po_service.controller;

import com.zynolo_nexus.po_service.dto.api.MessageResponseDTO;
import com.zynolo_nexus.po_service.dto.request.DashboardActionItemsRequest;
import com.zynolo_nexus.po_service.dto.request.DashboardReferenceDataRequest;
import com.zynolo_nexus.po_service.dto.request.DashboardStatusBreakdownRequest;
import com.zynolo_nexus.po_service.dto.request.DashboardSummaryRequest;
import com.zynolo_nexus.po_service.dto.request.DashboardTrendRequest;
import com.zynolo_nexus.po_service.dto.response.DashboardActionItemsDto;
import com.zynolo_nexus.po_service.dto.response.DashboardReferenceDataDto;
import com.zynolo_nexus.po_service.dto.response.DashboardStatusBreakdownDto;
import com.zynolo_nexus.po_service.dto.response.DashboardSummaryDto;
import com.zynolo_nexus.po_service.dto.response.DashboardTrendDto;
import com.zynolo_nexus.po_service.service.PurchaseOrderDashboardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/po/dashboard")
public class PurchaseOrderDashboardController {

    private final PurchaseOrderDashboardService purchaseOrderDashboardService;

    @PostMapping("/reference-data")
    public MessageResponseDTO<DashboardReferenceDataDto> referenceData(@Valid @RequestBody DashboardReferenceDataRequest request) {
        return success("Reference data PODB retrieved successfully", purchaseOrderDashboardService.getReferenceData(request));
    }

    @PostMapping("/summary")
    public MessageResponseDTO<DashboardSummaryDto> summary(@Valid @RequestBody DashboardSummaryRequest request) {
        return success("PO dashboard summary retrieved successfully", purchaseOrderDashboardService.getSummary(request));
    }

    @PostMapping("/status-breakdown")
    public MessageResponseDTO<DashboardStatusBreakdownDto> statusBreakdown(@Valid @RequestBody DashboardStatusBreakdownRequest request) {
        return success("PO dashboard status breakdown retrieved successfully", purchaseOrderDashboardService.getStatusBreakdown(request));
    }

    @PostMapping("/trend")
    public MessageResponseDTO<DashboardTrendDto> trend(@Valid @RequestBody DashboardTrendRequest request) {
        return success("PO dashboard trend retrieved successfully", purchaseOrderDashboardService.getTrend(request));
    }

    @PostMapping("/action-items")
    public MessageResponseDTO<DashboardActionItemsDto> actionItems(@Valid @RequestBody DashboardActionItemsRequest request) {
        return success("PO dashboard action items retrieved successfully", purchaseOrderDashboardService.getActionItems(request));
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
