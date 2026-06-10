package com.zynolo_nexus.meeting_room_booking_service.controller;

import com.zynolo_nexus.meeting_room_booking_service.dto.api.MessageResponseDTO;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.MeetingBookingFilterRequest;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.MeetingBookingIdRequest;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.MeetingBookingReferenceDataRequest;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.MeetingBookingSaveDraftRequest;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.MeetingBookingUpdateRequest;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.MeetingBookingDto;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.MeetingBookingFilterResultDto;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.MeetingBookingReferenceDataDto;
import com.zynolo_nexus.meeting_room_booking_service.service.MeetingBookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/meeting-room/bookings")
@RequiredArgsConstructor
public class MeetingBookingController {

    private final MeetingBookingService meetingBookingService;

    @PostMapping("/reference-data")
    public MessageResponseDTO<MeetingBookingReferenceDataDto> referenceData(
            @RequestBody(required = false) MeetingBookingReferenceDataRequest request) {
        return meetingBookingService.referenceData(request);
    }

    @PostMapping("/save-draft")
    public MessageResponseDTO<MeetingBookingDto> saveDraft(@Valid @RequestBody MeetingBookingSaveDraftRequest request) {
        return meetingBookingService.saveDraft(request);
    }

    @PostMapping("/update")
    public MessageResponseDTO<MeetingBookingDto> update(@Valid @RequestBody MeetingBookingUpdateRequest request) {
        return meetingBookingService.update(request);
    }

    @PostMapping("/submit")
    public MessageResponseDTO<MeetingBookingDto> submit(@RequestBody MeetingBookingIdRequest request) {
        return meetingBookingService.submit(request);
    }

    @PostMapping("/cancel")
    public MessageResponseDTO<MeetingBookingDto> cancel(@RequestBody MeetingBookingIdRequest request) {
        return meetingBookingService.cancel(request);
    }

    @PostMapping("/view")
    public MessageResponseDTO<MeetingBookingDto> view(@RequestBody MeetingBookingIdRequest request) {
        return meetingBookingService.view(request != null ? request.getId() : null);
    }

    @PostMapping("/filter-list")
    public MessageResponseDTO<MeetingBookingFilterResultDto> filterList(@RequestBody MeetingBookingFilterRequest request) {
        return meetingBookingService.filterList(request);
    }
}
