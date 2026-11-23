package com.zynolo_nexus.fleet_service.controller;

import com.zynolo_nexus.fleet_service.dto.api.MessageResponseDTO;
import com.zynolo_nexus.fleet_service.dto.request.AssignTripCreateRequest;
import com.zynolo_nexus.fleet_service.dto.request.AssignTripReferenceDataRequest;
import com.zynolo_nexus.fleet_service.dto.request.AssignTripUpdateRequest;
import com.zynolo_nexus.fleet_service.dto.request.AssignTripViewRequest;
import com.zynolo_nexus.fleet_service.dto.request.TransportRequestFilterRequest;
import com.zynolo_nexus.fleet_service.dto.request.TransportRequestViewRequest;
import com.zynolo_nexus.fleet_service.dto.response.AssignTripReferenceDataDto;
import com.zynolo_nexus.fleet_service.dto.response.TransportRequestDto;
import com.zynolo_nexus.fleet_service.dto.response.TransportRequestFilterResultDto;
import com.zynolo_nexus.fleet_service.dto.response.TripDto;
import com.zynolo_nexus.fleet_service.service.AssignTripService;
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
@RequestMapping("/api/v1/fleet/assign-trip")
@Tag(name = "FLAT - Assign Trip", description = "Page code FLAT (`FLEET_SCHEDULING`)")
public class AssignTripController {

    private final AssignTripService assignTripService;
    private final ResponseMessageHelper messages;

    @PostMapping("/reference-data")
    public MessageResponseDTO<AssignTripReferenceDataDto> referenceData(
            @Valid @RequestBody AssignTripReferenceDataRequest request
    ) {
        return success(messages.get("fleet.assign.trip.reference.success"), assignTripService.getReferenceData(request));
    }

    @PostMapping("/filter-list")
    public MessageResponseDTO<TransportRequestFilterResultDto> filterList(
            @Valid @RequestBody TransportRequestFilterRequest request
    ) {
        return success(messages.get("fleet.assign.trip.filter.success"), assignTripService.filterOpenRequests(request));
    }

    @PostMapping("/view-request")
    public MessageResponseDTO<TransportRequestDto> viewRequest(
            @Valid @RequestBody TransportRequestViewRequest request
    ) {
        return success(messages.get("fleet.assign.trip.view.request.success"), assignTripService.viewRequest(request));
    }

    @PostMapping("/add")
    public MessageResponseDTO<TripDto> add(@Valid @RequestBody AssignTripCreateRequest request) {
        return success(messages.get("fleet.assign.trip.add.success"), assignTripService.assign(request));
    }

    @PostMapping("/view")
    public MessageResponseDTO<TripDto> view(@Valid @RequestBody AssignTripViewRequest request) {
        return success(messages.get("fleet.assign.trip.view.success"), assignTripService.view(request));
    }

    @PostMapping("/update")
    public MessageResponseDTO<TripDto> update(@Valid @RequestBody AssignTripUpdateRequest request) {
        return success(messages.get("fleet.assign.trip.update.success"), assignTripService.update(request));
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