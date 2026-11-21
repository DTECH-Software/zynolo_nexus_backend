package com.zynolo_nexus.fleet_service.controller;

import com.zynolo_nexus.fleet_service.dto.api.MessageResponseDTO;
import com.zynolo_nexus.fleet_service.dto.request.TripDetailReferenceDataRequest;
import com.zynolo_nexus.fleet_service.dto.request.TripDetailViewRequest;
import com.zynolo_nexus.fleet_service.dto.response.TripDetailDto;
import com.zynolo_nexus.fleet_service.dto.response.TripDetailReferenceDataDto;
import com.zynolo_nexus.fleet_service.service.TripDetailService;
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
@RequestMapping("/api/v1/fleet/trip-detail")
@Tag(name = "FLTD - Trip Detail", description = "Page code FLTD (`FLEET_PASSENGER`) — own trip detail + last location / trail")
public class TripDetailController {

    private final TripDetailService tripDetailService;

    @PostMapping("/reference-data")
    public MessageResponseDTO<TripDetailReferenceDataDto> referenceData(
            @Valid @RequestBody TripDetailReferenceDataRequest request
    ) {
        return success("Reference data FLTD retrieved successfully", tripDetailService.getReferenceData(request));
    }

    @PostMapping("/view")
    public MessageResponseDTO<TripDetailDto> view(@Valid @RequestBody TripDetailViewRequest request) {
        return success("Trip detail retrieved successfully", tripDetailService.view(request));
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
