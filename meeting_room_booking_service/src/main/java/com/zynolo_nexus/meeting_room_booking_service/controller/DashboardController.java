package com.zynolo_nexus.meeting_room_booking_service.controller;

import com.zynolo_nexus.meeting_room_booking_service.dto.api.MessageResponseDTO;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.DashboardOverviewRequest;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.DashboardReferenceDataRequest;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.DashboardOverviewDto;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.DashboardReferenceDataDto;
import com.zynolo_nexus.meeting_room_booking_service.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/meeting-room/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @PostMapping("/reference-data")
    public MessageResponseDTO<DashboardReferenceDataDto> referenceData(
            @RequestBody(required = false) DashboardReferenceDataRequest request) {
        return dashboardService.referenceData(request);
    }

    @PostMapping("/overview")
    public MessageResponseDTO<DashboardOverviewDto> overview(@RequestBody DashboardOverviewRequest request) {
        return dashboardService.overview(request);
    }
}
