package com.zynolo_nexus.meeting_room_booking_service.controller;

import com.zynolo_nexus.meeting_room_booking_service.dto.api.MessageResponseDTO;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.CancellationFilterRequest;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.CancellationReferenceDataRequest;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.CancellationSubmitRequest;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.CancellationViewRequest;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.CancellationFilterResultDto;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.CancellationReferenceDataDto;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.MeetingBookingDto;
import com.zynolo_nexus.meeting_room_booking_service.service.CancellationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/meeting-room/cancellations")
@RequiredArgsConstructor
public class CancellationController {

    private final CancellationService cancellationService;

    @PostMapping("/reference-data")
    public MessageResponseDTO<CancellationReferenceDataDto> referenceData(
            @RequestBody(required = false) CancellationReferenceDataRequest request) {
        return cancellationService.referenceData(request);
    }

    @PostMapping("/filter-list")
    public MessageResponseDTO<CancellationFilterResultDto> filterList(@RequestBody CancellationFilterRequest request) {
        return cancellationService.filterList(request);
    }

    @PostMapping("/view")
    public MessageResponseDTO<MeetingBookingDto> view(@RequestBody CancellationViewRequest request) {
        return cancellationService.view(request);
    }

    @PostMapping("/cancel")
    public MessageResponseDTO<MeetingBookingDto> cancel(@RequestBody CancellationSubmitRequest request) {
        return cancellationService.cancel(request);
    }
}
