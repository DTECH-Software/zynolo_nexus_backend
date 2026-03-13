package com.zynolo_nexus.po_service.controller;

import com.zynolo_nexus.po_service.dto.api.MessageResponseDTO;
import com.zynolo_nexus.po_service.dto.request.PaymentFilterRequest;
import com.zynolo_nexus.po_service.dto.request.PaymentHistoryRequest;
import com.zynolo_nexus.po_service.dto.request.PaymentPayRequest;
import com.zynolo_nexus.po_service.dto.request.PaymentReferenceDataRequest;
import com.zynolo_nexus.po_service.dto.request.PaymentViewRequest;
import com.zynolo_nexus.po_service.dto.response.PaymentFilterResultDto;
import com.zynolo_nexus.po_service.dto.response.PaymentHistoryDto;
import com.zynolo_nexus.po_service.dto.response.PaymentReferenceDataDto;
import com.zynolo_nexus.po_service.dto.response.PaymentViewDto;
import com.zynolo_nexus.po_service.service.PurchaseOrderPaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/po/payment")
public class PurchaseOrderPaymentController {

    private final PurchaseOrderPaymentService purchaseOrderPaymentService;

    @PostMapping("/reference-data")
    public MessageResponseDTO<PaymentReferenceDataDto> referenceData(@Valid @RequestBody PaymentReferenceDataRequest request) {
        return success("Reference data POPY retrieved successfully", purchaseOrderPaymentService.getReferenceData(request));
    }

    @PostMapping("/filter-list")
    public MessageResponseDTO<PaymentFilterResultDto> filterList(@Valid @RequestBody PaymentFilterRequest request) {
        return success("Payment records filtered successfully", purchaseOrderPaymentService.filterList(request));
    }

    @PostMapping("/view")
    public MessageResponseDTO<PaymentViewDto> view(@Valid @RequestBody PaymentViewRequest request) {
        return success("Payment details retrieved successfully", purchaseOrderPaymentService.view(request));
    }

    @PostMapping("/pay")
    public MessageResponseDTO<PaymentViewDto> pay(@Valid @RequestBody PaymentPayRequest request) {
        return success("Payment recorded successfully", purchaseOrderPaymentService.pay(request));
    }

    @PostMapping("/history")
    public MessageResponseDTO<PaymentHistoryDto> history(@Valid @RequestBody PaymentHistoryRequest request) {
        return success("Payment history retrieved successfully", purchaseOrderPaymentService.history(request));
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
