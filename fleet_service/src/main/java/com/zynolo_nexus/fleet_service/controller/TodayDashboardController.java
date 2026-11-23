package com.zynolo_nexus.fleet_service.controller;

import com.zynolo_nexus.fleet_service.dto.api.MessageResponseDTO;
import com.zynolo_nexus.fleet_service.dto.request.TodayDashboardReferenceDataRequest;
import com.zynolo_nexus.fleet_service.dto.request.TodayDashboardViewRequest;
import com.zynolo_nexus.fleet_service.dto.response.TodayDashboardDto;
import com.zynolo_nexus.fleet_service.dto.response.TodayDashboardReferenceDataDto;
import com.zynolo_nexus.fleet_service.service.TodayDashboardService;
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
@RequestMapping("/api/v1/fleet/today-dashboard")
@Tag(name = "FLDB - Today Dashboard", description = "Page code FLDB (`FLEET_REPORT`) — ops snapshot for a report date")
public class TodayDashboardController {

    private final TodayDashboardService todayDashboardService;
    private final ResponseMessageHelper messages;

    @PostMapping("/reference-data")
    public MessageResponseDTO<TodayDashboardReferenceDataDto> referenceData(
            @Valid @RequestBody TodayDashboardReferenceDataRequest request
    ) {
        return success(messages.get("fleet.today.dashboard.reference.success"), todayDashboardService.getReferenceData(request));
    }

    @PostMapping("/view")
    public MessageResponseDTO<TodayDashboardDto> view(@Valid @RequestBody TodayDashboardViewRequest request) {
        return success(messages.get("fleet.today.dashboard.view.success"), todayDashboardService.view(request));
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
