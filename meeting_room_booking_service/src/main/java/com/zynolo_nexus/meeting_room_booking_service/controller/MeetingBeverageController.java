package com.zynolo_nexus.meeting_room_booking_service.controller;

import com.zynolo_nexus.meeting_room_booking_service.dto.api.MessageResponseDTO;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.MeetingBeverageActiveStatusRequest;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.MeetingBeverageCreateRequest;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.MeetingBeverageFilterRequest;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.MeetingBeverageReferenceDataRequest;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.MeetingBeverageUpdateRequest;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.MeetingBeverageViewRequest;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.MeetingBeverageDto;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.MeetingBeverageFilterResultDto;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.MeetingBeverageReferenceDataDto;
import com.zynolo_nexus.meeting_room_booking_service.service.MeetingBeverageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/meeting-room/beverages")
@RequiredArgsConstructor
public class MeetingBeverageController {

    private final MeetingBeverageService meetingBeverageService;

    @PostMapping
    public MessageResponseDTO<MeetingBeverageDto> create(@Valid @RequestBody MeetingBeverageCreateRequest request) {
        return meetingBeverageService.create(request);
    }

    @PostMapping("/update")
    public MessageResponseDTO<MeetingBeverageDto> update(@Valid @RequestBody MeetingBeverageUpdateRequest request) {
        return meetingBeverageService.update(request);
    }

    @PostMapping("/view")
    public MessageResponseDTO<MeetingBeverageDto> view(@RequestBody MeetingBeverageViewRequest request) {
        return meetingBeverageService.view(request != null ? request.getId() : null);
    }

    @PostMapping("/active-status")
    public MessageResponseDTO<MeetingBeverageDto> activeStatus(@RequestBody MeetingBeverageActiveStatusRequest request) {
        return meetingBeverageService.updateActiveStatus(request);
    }

    @PostMapping("/filter-list")
    public MessageResponseDTO<MeetingBeverageFilterResultDto> filterList(@RequestBody MeetingBeverageFilterRequest request) {
        return meetingBeverageService.filterList(request);
    }

    @PostMapping("/reference-data")
    public MessageResponseDTO<MeetingBeverageReferenceDataDto> referenceData(
            @RequestBody(required = false) MeetingBeverageReferenceDataRequest request) {
        return meetingBeverageService.referenceData(request);
    }
}
