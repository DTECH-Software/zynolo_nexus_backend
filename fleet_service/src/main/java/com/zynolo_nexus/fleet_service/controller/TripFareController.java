package com.zynolo_nexus.fleet_service.controller;

import com.zynolo_nexus.fleet_service.dto.api.MessageResponseDTO;
import com.zynolo_nexus.fleet_service.dto.request.TripFareCreateRequest;
import com.zynolo_nexus.fleet_service.dto.request.TripFareFilterRequest;
import com.zynolo_nexus.fleet_service.dto.request.TripFareReferenceDataRequest;
import com.zynolo_nexus.fleet_service.dto.request.TripFareUpdateRequest;
import com.zynolo_nexus.fleet_service.dto.request.TripFareViewRequest;
import com.zynolo_nexus.fleet_service.dto.response.TripFareDto;
import com.zynolo_nexus.fleet_service.dto.response.TripFareFilterResultDto;
import com.zynolo_nexus.fleet_service.dto.response.TripFareReferenceDataDto;
import com.zynolo_nexus.fleet_service.service.TripFareService;
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
@RequestMapping("/api/v1/fleet/trip-fare")
@Tag(name = "FLTF - Trip Fare", description = "Page code FLTF (`FLEET_PAYMENT`) — record fare on completed trips")
public class TripFareController {

    private final TripFareService tripFareService;

    @PostMapping("/reference-data")
    public MessageResponseDTO<TripFareReferenceDataDto> referenceData(
            @Valid @RequestBody TripFareReferenceDataRequest request
    ) {
        return success("Reference data FLTF retrieved successfully", tripFareService.getReferenceData(request));
    }

    @PostMapping("/filter-list")
    public MessageResponseDTO<TripFareFilterResultDto> filterList(
            @Valid @RequestBody TripFareFilterRequest request
    ) {
        return success("Trip fares filtered successfully", tripFareService.filterList(request));
    }

    @PostMapping("/add")
    public MessageResponseDTO<TripFareDto> add(@Valid @RequestBody TripFareCreateRequest request) {
        return success("Trip fare recorded successfully", tripFareService.create(request));
    }

    @PostMapping("/view")
    public MessageResponseDTO<TripFareDto> view(@Valid @RequestBody TripFareViewRequest request) {
        return success("Trip fare retrieved successfully", tripFareService.view(request));
    }

    @PostMapping("/update")
    public MessageResponseDTO<TripFareDto> update(@Valid @RequestBody TripFareUpdateRequest request) {
        return success("Trip fare updated successfully", tripFareService.update(request));
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
