package com.zynolo_nexus.fleet_service.controller;

import com.zynolo_nexus.fleet_service.dto.api.MessageResponseDTO;
import com.zynolo_nexus.fleet_service.dto.request.MyTripFilterRequest;
import com.zynolo_nexus.fleet_service.dto.request.MyTripReferenceDataRequest;
import com.zynolo_nexus.fleet_service.dto.request.MyTripViewRequest;
import com.zynolo_nexus.fleet_service.dto.response.MyTripReferenceDataDto;
import com.zynolo_nexus.fleet_service.dto.response.TripDto;
import com.zynolo_nexus.fleet_service.dto.response.TripFilterResultDto;
import com.zynolo_nexus.fleet_service.service.MyTripService;
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
@RequestMapping("/api/v1/fleet/my-trips")
@Tag(name = "FLMT - My Trips", description = "Page code FLMT (`FLEET_PASSENGER`)")
public class MyTripController {

    private final MyTripService myTripService;
    private final ResponseMessageHelper messages;

    @PostMapping("/reference-data")
    public MessageResponseDTO<MyTripReferenceDataDto> referenceData(
            @Valid @RequestBody MyTripReferenceDataRequest request
    ) {
        return success(messages.get("fleet.my.trips.reference.success"), myTripService.getReferenceData(request));
    }

    @PostMapping("/filter-list")
    public MessageResponseDTO<TripFilterResultDto> filterList(
            @Valid @RequestBody MyTripFilterRequest request
    ) {
        return success(messages.get("fleet.my.trips.filter.success"), myTripService.filterList(request));
    }

    @PostMapping("/view")
    public MessageResponseDTO<TripDto> view(@Valid @RequestBody MyTripViewRequest request) {
        return success(messages.get("fleet.my.trips.view.success"), myTripService.view(request));
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
