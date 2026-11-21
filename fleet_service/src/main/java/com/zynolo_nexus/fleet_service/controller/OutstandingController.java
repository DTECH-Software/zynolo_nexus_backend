package com.zynolo_nexus.fleet_service.controller;

import com.zynolo_nexus.fleet_service.dto.api.MessageResponseDTO;
import com.zynolo_nexus.fleet_service.dto.request.OutstandingFilterRequest;
import com.zynolo_nexus.fleet_service.dto.request.OutstandingReferenceDataRequest;
import com.zynolo_nexus.fleet_service.dto.request.OutstandingViewRequest;
import com.zynolo_nexus.fleet_service.dto.response.OutstandingDto;
import com.zynolo_nexus.fleet_service.dto.response.OutstandingFilterResultDto;
import com.zynolo_nexus.fleet_service.dto.response.OutstandingReferenceDataDto;
import com.zynolo_nexus.fleet_service.service.OutstandingService;
import com.zynolo_nexus.fleet_service.service.support.ResponseMessageHelper;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/fleet/outstanding")
@Tag(name = "FLOU - Outstanding", description = "Page code FLOU (`FLEET_REPORT`) — open requests, assigned trips, completed without fare")
public class OutstandingController {

    private final OutstandingService outstandingService;
    private final ResponseMessageHelper messages;

    @PostMapping("/reference-data")
    public MessageResponseDTO<OutstandingReferenceDataDto> referenceData(
            @Valid @RequestBody OutstandingReferenceDataRequest request
    ) {
        return success(messages.get("fleet.outstanding.reference.success"), outstandingService.getReferenceData(request));
    }

    @PostMapping("/filter-list")
    public MessageResponseDTO<OutstandingFilterResultDto> filterList(
            @Valid @RequestBody OutstandingFilterRequest request
    ) {
        return success(messages.get("fleet.outstanding.filter.success"), outstandingService.filterList(request));
    }

    @PostMapping("/view")
    public MessageResponseDTO<OutstandingDto> view(@Valid @RequestBody OutstandingViewRequest request) {
        return success(messages.get("fleet.outstanding.view.success"), outstandingService.view(request));
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
