package com.zynolo_nexus.meeting_room_booking_service.controller;

import com.zynolo_nexus.meeting_room_booking_service.dto.api.MessageResponseDTO;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.OngoingUpdateFilterRequest;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.OngoingUpdateReferenceDataRequest;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.OngoingUpdateSaveRequest;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.OngoingUpdateViewRequest;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.MeetingBookingDto;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.OngoingUpdateFilterResultDto;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.OngoingUpdateReferenceDataDto;
import com.zynolo_nexus.meeting_room_booking_service.service.OngoingUpdateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/meeting-room/ongoing-updates")
@RequiredArgsConstructor
public class OngoingUpdateController {

    private final OngoingUpdateService ongoingUpdateService;

    @PostMapping("/reference-data")
    public MessageResponseDTO<OngoingUpdateReferenceDataDto> referenceData(
            @RequestBody(required = false) OngoingUpdateReferenceDataRequest request) {
        return ongoingUpdateService.referenceData(request);
    }

    @PostMapping("/filter-list")
    public MessageResponseDTO<OngoingUpdateFilterResultDto> filterList(@RequestBody OngoingUpdateFilterRequest request) {
        return ongoingUpdateService.filterList(request);
    }

    @PostMapping("/view")
    public MessageResponseDTO<MeetingBookingDto> view(@RequestBody OngoingUpdateViewRequest request) {
        return ongoingUpdateService.view(request);
    }

    @PostMapping("/save-update")
    public MessageResponseDTO<MeetingBookingDto> saveUpdate(@Valid @RequestBody OngoingUpdateSaveRequest request) {
        return ongoingUpdateService.saveUpdate(request);
    }
}
