package com.zynolo_nexus.meeting_room_booking_service.controller;

import com.zynolo_nexus.meeting_room_booking_service.dto.api.MessageResponseDTO;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.MeetingSupportServiceActiveStatusRequest;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.MeetingSupportServiceCreateRequest;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.MeetingSupportServiceFilterRequest;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.MeetingSupportServiceReferenceDataRequest;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.MeetingSupportServiceUpdateRequest;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.MeetingSupportServiceViewRequest;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.MeetingSupportServiceDto;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.MeetingSupportServiceFilterResultDto;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.MeetingSupportServiceReferenceDataDto;
import com.zynolo_nexus.meeting_room_booking_service.service.MeetingSupportServiceMasterService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/meeting-room/support-services")
@RequiredArgsConstructor
public class MeetingSupportServiceController {

    private final MeetingSupportServiceMasterService meetingSupportServiceMasterService;

    @PostMapping
    public MessageResponseDTO<MeetingSupportServiceDto> create(@Valid @RequestBody MeetingSupportServiceCreateRequest request) {
        return meetingSupportServiceMasterService.create(request);
    }

    @PostMapping("/update")
    public MessageResponseDTO<MeetingSupportServiceDto> update(@Valid @RequestBody MeetingSupportServiceUpdateRequest request) {
        return meetingSupportServiceMasterService.update(request);
    }

    @PostMapping("/view")
    public MessageResponseDTO<MeetingSupportServiceDto> view(@RequestBody MeetingSupportServiceViewRequest request) {
        return meetingSupportServiceMasterService.view(request != null ? request.getId() : null);
    }

    @PostMapping("/active-status")
    public MessageResponseDTO<MeetingSupportServiceDto> activeStatus(@RequestBody MeetingSupportServiceActiveStatusRequest request) {
        return meetingSupportServiceMasterService.updateActiveStatus(request);
    }

    @PostMapping("/filter-list")
    public MessageResponseDTO<MeetingSupportServiceFilterResultDto> filterList(@RequestBody MeetingSupportServiceFilterRequest request) {
        return meetingSupportServiceMasterService.filterList(request);
    }

    @PostMapping("/reference-data")
    public MessageResponseDTO<MeetingSupportServiceReferenceDataDto> referenceData(
            @RequestBody(required = false) MeetingSupportServiceReferenceDataRequest request) {
        return meetingSupportServiceMasterService.referenceData(request);
    }
}
