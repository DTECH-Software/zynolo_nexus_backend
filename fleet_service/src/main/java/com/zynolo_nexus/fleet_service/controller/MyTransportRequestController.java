package com.zynolo_nexus.fleet_service.controller;

import com.zynolo_nexus.fleet_service.dto.api.MessageResponseDTO;
import com.zynolo_nexus.fleet_service.dto.request.MyTransportRequestCancelRequest;
import com.zynolo_nexus.fleet_service.dto.request.MyTransportRequestReferenceDataRequest;
import com.zynolo_nexus.fleet_service.dto.request.TransportRequestFilterRequest;
import com.zynolo_nexus.fleet_service.dto.request.TransportRequestViewRequest;
import com.zynolo_nexus.fleet_service.dto.response.MyTransportRequestReferenceDataDto;
import com.zynolo_nexus.fleet_service.dto.response.TransportRequestDto;
import com.zynolo_nexus.fleet_service.dto.response.TransportRequestFilterResultDto;
import com.zynolo_nexus.fleet_service.service.MyTransportRequestService;
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
@RequestMapping("/api/v1/fleet/my-requests")
@Tag(name = "FLMR - My Requests", description = "Page code FLMR (`FLEET_REQUEST`)")
public class MyTransportRequestController {

    private final MyTransportRequestService myTransportRequestService;

    @PostMapping("/reference-data")
    public MessageResponseDTO<MyTransportRequestReferenceDataDto> referenceData(
            @Valid @RequestBody MyTransportRequestReferenceDataRequest request
    ) {
        return success("Reference data FLMR retrieved successfully", myTransportRequestService.getReferenceData(request));
    }

    @PostMapping("/filter-list")
    public MessageResponseDTO<TransportRequestFilterResultDto> filterList(
            @Valid @RequestBody TransportRequestFilterRequest request
    ) {
        return success("My transport requests filtered successfully", myTransportRequestService.filterList(request));
    }

    @PostMapping("/view")
    public MessageResponseDTO<TransportRequestDto> view(@Valid @RequestBody TransportRequestViewRequest request) {
        return success("Transport request retrieved successfully", myTransportRequestService.view(request));
    }

    @PostMapping("/cancel")
    public MessageResponseDTO<TransportRequestDto> cancel(@Valid @RequestBody MyTransportRequestCancelRequest request) {
        return success("Transport request cancelled successfully", myTransportRequestService.cancel(request));
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