package com.zynolo_nexus.po_service.controller;

import com.zynolo_nexus.po_service.dto.api.MessageResponseDTO;
import com.zynolo_nexus.po_service.dto.request.GoodsReceiptFilterRequest;
import com.zynolo_nexus.po_service.dto.request.GoodsReceiptHistoryRequest;
import com.zynolo_nexus.po_service.dto.request.GoodsReceiptReceiveRequest;
import com.zynolo_nexus.po_service.dto.request.GoodsReceiptReferenceDataRequest;
import com.zynolo_nexus.po_service.dto.request.GoodsReceiptViewRequest;
import com.zynolo_nexus.po_service.dto.response.GoodsReceiptFilterResultDto;
import com.zynolo_nexus.po_service.dto.response.GoodsReceiptHistoryDto;
import com.zynolo_nexus.po_service.dto.response.GoodsReceiptReferenceDataDto;
import com.zynolo_nexus.po_service.dto.response.GoodsReceiptViewDto;
import com.zynolo_nexus.po_service.service.GoodsReceiptService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/po/goods-receipt")
public class GoodsReceiptController {

    private final GoodsReceiptService goodsReceiptService;

    @PostMapping("/reference-data")
    public MessageResponseDTO<GoodsReceiptReferenceDataDto> referenceData(@Valid @RequestBody GoodsReceiptReferenceDataRequest request) {
        return success("Reference data POGR retrieved successfully", goodsReceiptService.getReferenceData(request));
    }

    @PostMapping("/filter-list")
    public MessageResponseDTO<GoodsReceiptFilterResultDto> filterList(@Valid @RequestBody GoodsReceiptFilterRequest request) {
        return success("Goods receipts filtered successfully", goodsReceiptService.filterList(request));
    }

    @PostMapping("/view")
    public MessageResponseDTO<GoodsReceiptViewDto> view(@Valid @RequestBody GoodsReceiptViewRequest request) {
        return success("Goods receipt details retrieved successfully", goodsReceiptService.view(request));
    }

    @PostMapping("/receive")
    public MessageResponseDTO<GoodsReceiptViewDto> receive(@Valid @RequestBody GoodsReceiptReceiveRequest request) {
        return success("Goods receipt recorded successfully", goodsReceiptService.receive(request));
    }

    @PostMapping("/history")
    public MessageResponseDTO<GoodsReceiptHistoryDto> history(@Valid @RequestBody GoodsReceiptHistoryRequest request) {
        return success("Goods receipt history retrieved successfully", goodsReceiptService.history(request));
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
