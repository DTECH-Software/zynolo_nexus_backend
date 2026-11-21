package com.zynolo_nexus.fleet_service.controller;

import com.zynolo_nexus.fleet_service.dto.api.MessageResponseDTO;
import com.zynolo_nexus.fleet_service.dto.request.LiveTrackFilterRequest;
import com.zynolo_nexus.fleet_service.dto.request.LiveTrackReferenceDataRequest;
import com.zynolo_nexus.fleet_service.dto.request.LiveTrackViewRequest;
import com.zynolo_nexus.fleet_service.dto.response.LiveTrackDto;
import com.zynolo_nexus.fleet_service.dto.response.LiveTrackFilterResultDto;
import com.zynolo_nexus.fleet_service.dto.response.LiveTrackReferenceDataDto;
import com.zynolo_nexus.fleet_service.service.LiveTrackService;
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
@RequestMapping("/api/v1/fleet/live-track")
@Tag(name = "FLLT - Live Trip Track", description = "Page code FLLT (`FLEET_TRACKING`) — poll every recommendedPollSeconds until WebSocket is enabled")
public class LiveTrackController {

    private final LiveTrackService liveTrackService;
    private final ResponseMessageHelper messages;

    @PostMapping("/reference-data")
    public MessageResponseDTO<LiveTrackReferenceDataDto> referenceData(
            @Valid @RequestBody LiveTrackReferenceDataRequest request
    ) {
        return success(messages.get("fleet.live.track.reference.success"), liveTrackService.getReferenceData(request));
    }

    @PostMapping("/filter-list")
    public MessageResponseDTO<LiveTrackFilterResultDto> filterList(
            @Valid @RequestBody LiveTrackFilterRequest request
    ) {
        return success(messages.get("fleet.live.track.filter.success"), liveTrackService.filterList(request));
    }

    @PostMapping("/view")
    public MessageResponseDTO<LiveTrackDto> view(@Valid @RequestBody LiveTrackViewRequest request) {
        return success(messages.get("fleet.live.track.view.success"), liveTrackService.view(request));
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