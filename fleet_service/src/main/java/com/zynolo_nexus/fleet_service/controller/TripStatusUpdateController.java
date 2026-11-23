package com.zynolo_nexus.fleet_service.controller;

import com.zynolo_nexus.fleet_service.dto.api.MessageResponseDTO;
import com.zynolo_nexus.fleet_service.dto.request.TripStatusFilterRequest;
import com.zynolo_nexus.fleet_service.dto.request.TripStatusReferenceDataRequest;
import com.zynolo_nexus.fleet_service.dto.request.TripStatusUpdateRequest;
import com.zynolo_nexus.fleet_service.dto.request.TripStatusViewRequest;
import com.zynolo_nexus.fleet_service.dto.response.TripDto;
import com.zynolo_nexus.fleet_service.dto.response.TripFilterResultDto;
import com.zynolo_nexus.fleet_service.dto.response.TripStatusReferenceDataDto;
import com.zynolo_nexus.fleet_service.service.TripStatusUpdateService;
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
@RequestMapping("/api/v1/fleet/trip-status")
@Tag(name = "FLTSU - Trip Status Update", description = "Page code FLTSU (`FLEET_DRIVER_OPS`)")
public class TripStatusUpdateController {

    private final TripStatusUpdateService tripStatusUpdateService;
    private final ResponseMessageHelper messages;

    @PostMapping("/reference-data")
    public MessageResponseDTO<TripStatusReferenceDataDto> referenceData(
            @Valid @RequestBody TripStatusReferenceDataRequest request
    ) {
        return success(messages.get("fleet.trip.status.reference.success"), tripStatusUpdateService.getReferenceData(request));
    }

    @PostMapping("/filter-list")
    public MessageResponseDTO<TripFilterResultDto> filterList(
            @Valid @RequestBody TripStatusFilterRequest request
    ) {
        return success(messages.get("fleet.trip.status.filter.success"), tripStatusUpdateService.filterList(request));
    }

    @PostMapping("/view")
    public MessageResponseDTO<TripDto> view(@Valid @RequestBody TripStatusViewRequest request) {
        return success(messages.get("fleet.trip.status.view.success"), tripStatusUpdateService.view(request));
    }

    @PostMapping("/status-update")
    public MessageResponseDTO<TripDto> statusUpdate(@Valid @RequestBody TripStatusUpdateRequest request) {
        return success(messages.get("fleet.trip.status.update.success"), tripStatusUpdateService.updateStatus(request));
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