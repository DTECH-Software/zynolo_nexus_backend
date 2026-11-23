package com.zynolo_nexus.fleet_service.controller;

import com.zynolo_nexus.fleet_service.dto.api.MessageResponseDTO;
import com.zynolo_nexus.fleet_service.dto.request.RequestQueueReferenceDataRequest;
import com.zynolo_nexus.fleet_service.dto.request.RequestQueueUpdateRequest;
import com.zynolo_nexus.fleet_service.dto.request.TransportRequestFilterRequest;
import com.zynolo_nexus.fleet_service.dto.request.TransportRequestViewRequest;
import com.zynolo_nexus.fleet_service.dto.response.RequestQueueReferenceDataDto;
import com.zynolo_nexus.fleet_service.dto.response.TransportRequestDto;
import com.zynolo_nexus.fleet_service.dto.response.TransportRequestFilterResultDto;
import com.zynolo_nexus.fleet_service.service.RequestQueueService;
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
@RequestMapping("/api/v1/fleet/request-queue")
@Tag(name = "FLRQ - Request Queue", description = "Page code FLRQ (`FLEET_REQUEST`)")
public class RequestQueueController {

    private final RequestQueueService requestQueueService;
    private final ResponseMessageHelper messages;

    @PostMapping("/reference-data")
    public MessageResponseDTO<RequestQueueReferenceDataDto> referenceData(
            @Valid @RequestBody RequestQueueReferenceDataRequest request
    ) {
        return success(messages.get("fleet.request.queue.reference.success"), requestQueueService.getReferenceData(request));
    }

    @PostMapping("/filter-list")
    public MessageResponseDTO<TransportRequestFilterResultDto> filterList(
            @Valid @RequestBody TransportRequestFilterRequest request
    ) {
        return success(messages.get("fleet.request.queue.filter.success"), requestQueueService.filterList(request));
    }

    @PostMapping("/view")
    public MessageResponseDTO<TransportRequestDto> view(@Valid @RequestBody TransportRequestViewRequest request) {
        return success(messages.get("fleet.request.queue.view.success"), requestQueueService.view(request));
    }

    @PostMapping("/update")
    public MessageResponseDTO<TransportRequestDto> update(@Valid @RequestBody RequestQueueUpdateRequest request) {
        return success(messages.get("fleet.request.queue.update.success"), requestQueueService.update(request));
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