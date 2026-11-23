package com.zynolo_nexus.po_service.controller;

import com.zynolo_nexus.po_service.dto.api.MessageResponseDTO;
import com.zynolo_nexus.po_service.dto.request.CurrencyCreateRequest;
import com.zynolo_nexus.po_service.dto.request.CurrencyFilterRequest;
import com.zynolo_nexus.po_service.dto.request.CurrencyReferenceDataRequest;
import com.zynolo_nexus.po_service.dto.request.CurrencyStatusRequest;
import com.zynolo_nexus.po_service.dto.request.CurrencyUpdateRequest;
import com.zynolo_nexus.po_service.dto.request.CurrencyViewRequest;
import com.zynolo_nexus.po_service.dto.response.CurrencyDto;
import com.zynolo_nexus.po_service.dto.response.CurrencyFilterResultDto;
import com.zynolo_nexus.po_service.dto.response.CurrencyReferenceDataDto;
import com.zynolo_nexus.po_service.service.CurrencyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/po/currencies")
public class CurrencyController {

    private final CurrencyService currencyService;

    @PostMapping("/reference-data")
    public MessageResponseDTO<CurrencyReferenceDataDto> referenceData(@Valid @RequestBody CurrencyReferenceDataRequest request) {
        return success("Reference data CURM retrieved successfully", currencyService.getReferenceData(request));
    }

    @PostMapping
    public MessageResponseDTO<CurrencyDto> create(@Valid @RequestBody CurrencyCreateRequest request) {
        return success("Currency created successfully", currencyService.create(request));
    }

    @PostMapping("/view")
    public MessageResponseDTO<CurrencyDto> view(@Valid @RequestBody CurrencyViewRequest request) {
        return success("Currency retrieved successfully", currencyService.view(request));
    }

    @PostMapping("/update")
    public MessageResponseDTO<CurrencyDto> update(@Valid @RequestBody CurrencyUpdateRequest request) {
        return success("Currency updated successfully", currencyService.update(request));
    }

    @PostMapping("/status")
    public MessageResponseDTO<CurrencyDto> updateStatus(@Valid @RequestBody CurrencyStatusRequest request) {
        return success("Currency status updated successfully", currencyService.updateStatus(request));
    }

    @PostMapping("/filter-list")
    public MessageResponseDTO<CurrencyFilterResultDto> filterList(@Valid @RequestBody CurrencyFilterRequest request) {
        return success("Currencies filtered successfully", currencyService.filterList(request));
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
