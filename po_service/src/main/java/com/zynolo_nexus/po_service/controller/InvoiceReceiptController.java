package com.zynolo_nexus.po_service.controller;

import com.zynolo_nexus.po_service.dto.api.MessageResponseDTO;
import com.zynolo_nexus.po_service.dto.request.InvoiceReceiptFilterRequest;
import com.zynolo_nexus.po_service.dto.request.InvoiceReceiptHistoryRequest;
import com.zynolo_nexus.po_service.dto.request.InvoiceReceiptReceiveRequest;
import com.zynolo_nexus.po_service.dto.request.InvoiceReceiptReferenceDataRequest;
import com.zynolo_nexus.po_service.dto.request.InvoiceReceiptViewRequest;
import com.zynolo_nexus.po_service.dto.response.InvoiceReceiptFilterResultDto;
import com.zynolo_nexus.po_service.dto.response.InvoiceReceiptHistoryDto;
import com.zynolo_nexus.po_service.dto.response.InvoiceReceiptReferenceDataDto;
import com.zynolo_nexus.po_service.dto.response.InvoiceReceiptViewDto;
import com.zynolo_nexus.po_service.service.InvoiceReceiptService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/po/invoice-receipt")
public class InvoiceReceiptController {

    private final InvoiceReceiptService invoiceReceiptService;

    @PostMapping("/reference-data")
    public MessageResponseDTO<InvoiceReceiptReferenceDataDto> referenceData(@Valid @RequestBody InvoiceReceiptReferenceDataRequest request) {
        return success("Reference data POIR retrieved successfully", invoiceReceiptService.getReferenceData(request));
    }

    @PostMapping("/filter-list")
    public MessageResponseDTO<InvoiceReceiptFilterResultDto> filterList(@Valid @RequestBody InvoiceReceiptFilterRequest request) {
        return success("Invoice receipts filtered successfully", invoiceReceiptService.filterList(request));
    }

    @PostMapping("/view")
    public MessageResponseDTO<InvoiceReceiptViewDto> view(@Valid @RequestBody InvoiceReceiptViewRequest request) {
        return success("Invoice receipt details retrieved successfully", invoiceReceiptService.view(request));
    }

    @PostMapping("/receive")
    public MessageResponseDTO<InvoiceReceiptViewDto> receive(@Valid @RequestBody InvoiceReceiptReceiveRequest request) {
        return success("Invoice received successfully", invoiceReceiptService.receive(request));
    }

    @PostMapping("/history")
    public MessageResponseDTO<InvoiceReceiptHistoryDto> history(@Valid @RequestBody InvoiceReceiptHistoryRequest request) {
        return success("Invoice receipt history retrieved successfully", invoiceReceiptService.history(request));
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
