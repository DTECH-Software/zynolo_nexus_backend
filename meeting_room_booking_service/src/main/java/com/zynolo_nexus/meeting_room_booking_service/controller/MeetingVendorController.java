package com.zynolo_nexus.meeting_room_booking_service.controller;

import com.zynolo_nexus.meeting_room_booking_service.dto.api.MessageResponseDTO;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.MeetingVendorActiveStatusRequest;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.MeetingVendorCreateRequest;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.MeetingVendorFilterRequest;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.MeetingVendorReferenceDataRequest;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.MeetingVendorUpdateRequest;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.MeetingVendorViewRequest;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.MeetingVendorDto;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.MeetingVendorFilterResultDto;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.MeetingVendorReferenceDataDto;
import com.zynolo_nexus.meeting_room_booking_service.service.MeetingVendorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/meeting-room/vendors")
@RequiredArgsConstructor
public class MeetingVendorController {

    private final MeetingVendorService meetingVendorService;

    @PostMapping
    public MessageResponseDTO<MeetingVendorDto> create(@Valid @RequestBody MeetingVendorCreateRequest request) {
        return meetingVendorService.create(request);
    }

    @PostMapping("/update")
    public MessageResponseDTO<MeetingVendorDto> update(@Valid @RequestBody MeetingVendorUpdateRequest request) {
        return meetingVendorService.update(request);
    }

    @PostMapping("/view")
    public MessageResponseDTO<MeetingVendorDto> view(@RequestBody MeetingVendorViewRequest request) {
        return meetingVendorService.view(request != null ? request.getId() : null);
    }

    @PostMapping("/active-status")
    public MessageResponseDTO<MeetingVendorDto> activeStatus(@RequestBody MeetingVendorActiveStatusRequest request) {
        return meetingVendorService.updateActiveStatus(request);
    }

    @PostMapping("/filter-list")
    public MessageResponseDTO<MeetingVendorFilterResultDto> filterList(@RequestBody MeetingVendorFilterRequest request) {
        return meetingVendorService.filterList(request);
    }

    @PostMapping("/reference-data")
    public MessageResponseDTO<MeetingVendorReferenceDataDto> referenceData(
            @RequestBody(required = false) MeetingVendorReferenceDataRequest request) {
        return meetingVendorService.referenceData(request);
    }
}
