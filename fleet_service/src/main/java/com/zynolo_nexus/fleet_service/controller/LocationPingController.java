package com.zynolo_nexus.fleet_service.controller;

import com.zynolo_nexus.fleet_service.dto.api.MessageResponseDTO;
import com.zynolo_nexus.fleet_service.dto.request.LocationPingCreateRequest;
import com.zynolo_nexus.fleet_service.dto.request.LocationPingReferenceDataRequest;
import com.zynolo_nexus.fleet_service.dto.response.LocationPingDto;
import com.zynolo_nexus.fleet_service.dto.response.LocationPingReferenceDataDto;
import com.zynolo_nexus.fleet_service.service.LocationPingService;
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
@RequestMapping("/api/v1/fleet/location-ping")
@Tag(name = "FLLP - Location Ping", description = "Page code FLLP (`FLEET_TRACKING`) — driver GPS ingest (REST; WebSocket later)")
public class LocationPingController {

    private final LocationPingService locationPingService;
    private final ResponseMessageHelper messages;

    @PostMapping("/reference-data")
    public MessageResponseDTO<LocationPingReferenceDataDto> referenceData(
            @Valid @RequestBody LocationPingReferenceDataRequest request
    ) {
        return success(messages.get("fleet.location.ping.reference.success"), locationPingService.getReferenceData(request));
    }

    @PostMapping("/add")
    public MessageResponseDTO<LocationPingDto> add(@Valid @RequestBody LocationPingCreateRequest request) {
        return success(messages.get("fleet.location.ping.add.success"), locationPingService.ping(request));
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