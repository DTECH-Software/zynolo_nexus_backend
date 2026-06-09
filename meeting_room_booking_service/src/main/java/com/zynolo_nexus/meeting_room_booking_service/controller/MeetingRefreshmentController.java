package com.zynolo_nexus.meeting_room_booking_service.controller;

import com.zynolo_nexus.meeting_room_booking_service.dto.api.MessageResponseDTO;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.MeetingRefreshmentActiveStatusRequest;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.MeetingRefreshmentCreateRequest;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.MeetingRefreshmentFilterRequest;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.MeetingRefreshmentReferenceDataRequest;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.MeetingRefreshmentUpdateRequest;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.MeetingRefreshmentViewRequest;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.MeetingRefreshmentDto;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.MeetingRefreshmentFilterResultDto;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.MeetingRefreshmentReferenceDataDto;
import com.zynolo_nexus.meeting_room_booking_service.service.MeetingRefreshmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/meeting-room/refreshments")
@RequiredArgsConstructor
public class MeetingRefreshmentController {

    private final MeetingRefreshmentService meetingRefreshmentService;

    @PostMapping
    public MessageResponseDTO<MeetingRefreshmentDto> create(@Valid @RequestBody MeetingRefreshmentCreateRequest request) {
        return meetingRefreshmentService.create(request);
    }

    @PostMapping("/update")
    public MessageResponseDTO<MeetingRefreshmentDto> update(@Valid @RequestBody MeetingRefreshmentUpdateRequest request) {
        return meetingRefreshmentService.update(request);
    }

    @PostMapping("/view")
    public MessageResponseDTO<MeetingRefreshmentDto> view(@RequestBody MeetingRefreshmentViewRequest request) {
        return meetingRefreshmentService.view(request != null ? request.getId() : null);
    }

    @PostMapping("/active-status")
    public MessageResponseDTO<MeetingRefreshmentDto> activeStatus(@RequestBody MeetingRefreshmentActiveStatusRequest request) {
        return meetingRefreshmentService.updateActiveStatus(request);
    }

    @PostMapping("/filter-list")
    public MessageResponseDTO<MeetingRefreshmentFilterResultDto> filterList(@RequestBody MeetingRefreshmentFilterRequest request) {
        return meetingRefreshmentService.filterList(request);
    }

    @PostMapping("/reference-data")
    public MessageResponseDTO<MeetingRefreshmentReferenceDataDto> referenceData(
            @RequestBody(required = false) MeetingRefreshmentReferenceDataRequest request) {
        return meetingRefreshmentService.referenceData(request);
    }
}
