package com.zynolo_nexus.fleet_service.controller;

import com.zynolo_nexus.fleet_service.dto.api.MessageResponseDTO;
import com.zynolo_nexus.fleet_service.dto.request.TodayScheduleFilterRequest;
import com.zynolo_nexus.fleet_service.dto.request.TodayScheduleReferenceDataRequest;
import com.zynolo_nexus.fleet_service.dto.request.TodayScheduleUpdateRequest;
import com.zynolo_nexus.fleet_service.dto.request.TodayScheduleViewRequest;
import com.zynolo_nexus.fleet_service.dto.response.TodayScheduleReferenceDataDto;
import com.zynolo_nexus.fleet_service.dto.response.TripDto;
import com.zynolo_nexus.fleet_service.dto.response.TripFilterResultDto;
import com.zynolo_nexus.fleet_service.service.TodayScheduleService;
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
@RequestMapping("/api/v1/fleet/today-schedule")
@Tag(name = "FLTS - Today's Schedule", description = "Page code FLTS (`FLEET_SCHEDULING`)")
public class TodayScheduleController {

    private final TodayScheduleService todayScheduleService;

    @PostMapping("/reference-data")
    public MessageResponseDTO<TodayScheduleReferenceDataDto> referenceData(
            @Valid @RequestBody TodayScheduleReferenceDataRequest request
    ) {
        return success("Reference data FLTS retrieved successfully", todayScheduleService.getReferenceData(request));
    }

    @PostMapping("/filter-list")
    public MessageResponseDTO<TripFilterResultDto> filterList(
            @Valid @RequestBody TodayScheduleFilterRequest request
    ) {
        return success("Today schedule filtered successfully", todayScheduleService.filterList(request));
    }

    @PostMapping("/view")
    public MessageResponseDTO<TripDto> view(@Valid @RequestBody TodayScheduleViewRequest request) {
        return success("Trip retrieved successfully", todayScheduleService.view(request));
    }

    @PostMapping("/update")
    public MessageResponseDTO<TripDto> update(@Valid @RequestBody TodayScheduleUpdateRequest request) {
        return success("Trip schedule updated successfully", todayScheduleService.update(request));
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