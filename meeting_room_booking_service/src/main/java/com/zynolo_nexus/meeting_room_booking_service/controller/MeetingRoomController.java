package com.zynolo_nexus.meeting_room_booking_service.controller;

import com.zynolo_nexus.meeting_room_booking_service.dto.api.MessageResponseDTO;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.MeetingRoomActiveStatusRequest;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.MeetingRoomCreateRequest;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.MeetingRoomFilterRequest;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.MeetingRoomReferenceDataRequest;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.MeetingRoomUpdateRequest;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.MeetingRoomViewRequest;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.MeetingRoomDto;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.MeetingRoomFilterResultDto;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.MeetingRoomReferenceDataDto;
import com.zynolo_nexus.meeting_room_booking_service.service.MeetingRoomService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/meeting-room/rooms")
@RequiredArgsConstructor
public class MeetingRoomController {

    private final MeetingRoomService meetingRoomService;

    @PostMapping
    public MessageResponseDTO<MeetingRoomDto> create(@Valid @RequestBody MeetingRoomCreateRequest request) {
        return meetingRoomService.create(request);
    }

    @PostMapping("/update")
    public MessageResponseDTO<MeetingRoomDto> update(@RequestBody MeetingRoomUpdateRequest request) {
        return meetingRoomService.update(request);
    }

    @PostMapping("/view")
    public MessageResponseDTO<MeetingRoomDto> view(@RequestBody MeetingRoomViewRequest request) {
        return meetingRoomService.view(request != null ? request.getId() : null);
    }

    @PostMapping("/active-status")
    public MessageResponseDTO<MeetingRoomDto> activeStatus(@RequestBody MeetingRoomActiveStatusRequest request) {
        return meetingRoomService.updateActiveStatus(request);
    }

    @PostMapping("/filter-list")
    public MessageResponseDTO<MeetingRoomFilterResultDto> filterList(@RequestBody MeetingRoomFilterRequest request) {
        return meetingRoomService.filterList(request);
    }

    @PostMapping("/reference-data")
    public MessageResponseDTO<MeetingRoomReferenceDataDto> referenceData(
            @RequestBody(required = false) MeetingRoomReferenceDataRequest request) {
        return meetingRoomService.referenceData(request);
    }
}
