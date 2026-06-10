package com.zynolo_nexus.meeting_room_booking_service.controller;

import com.zynolo_nexus.meeting_room_booking_service.dto.api.MessageResponseDTO;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.MeetingBookingUpdateRequest;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.MyMeetingRequestActionRequest;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.MyMeetingRequestFilterRequest;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.MyMeetingRequestReferenceDataRequest;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.MeetingBookingDto;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.MyMeetingRequestFilterResultDto;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.MyMeetingRequestReferenceDataDto;
import com.zynolo_nexus.meeting_room_booking_service.service.MyMeetingRequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/meeting-room/my-requests")
@RequiredArgsConstructor
public class MyMeetingRequestController {

    private final MyMeetingRequestService myMeetingRequestService;

    @PostMapping("/reference-data")
    public MessageResponseDTO<MyMeetingRequestReferenceDataDto> referenceData(
            @RequestBody(required = false) MyMeetingRequestReferenceDataRequest request) {
        return myMeetingRequestService.referenceData(request);
    }

    @PostMapping("/filter-list")
    public MessageResponseDTO<MyMeetingRequestFilterResultDto> filterList(@RequestBody MyMeetingRequestFilterRequest request) {
        return myMeetingRequestService.filterList(request);
    }

    @PostMapping("/view")
    public MessageResponseDTO<MeetingBookingDto> view(@RequestBody MyMeetingRequestActionRequest request) {
        return myMeetingRequestService.view(request);
    }

    @PostMapping("/update")
    public MessageResponseDTO<MeetingBookingDto> update(@Valid @RequestBody MeetingBookingUpdateRequest request) {
        return myMeetingRequestService.update(request);
    }

    @PostMapping("/submit")
    public MessageResponseDTO<MeetingBookingDto> submit(@RequestBody MyMeetingRequestActionRequest request) {
        return myMeetingRequestService.submit(request);
    }

    @PostMapping("/cancel")
    public MessageResponseDTO<MeetingBookingDto> cancel(@RequestBody MyMeetingRequestActionRequest request) {
        return myMeetingRequestService.cancel(request);
    }

    @PostMapping("/delete-draft")
    public MessageResponseDTO<MeetingBookingDto> deleteDraft(@RequestBody MyMeetingRequestActionRequest request) {
        return myMeetingRequestService.deleteDraft(request);
    }

    @PostMapping("/copy-as-new")
    public MessageResponseDTO<MeetingBookingDto> copyAsNew(@RequestBody MyMeetingRequestActionRequest request) {
        return myMeetingRequestService.copyAsNew(request);
    }

    @PostMapping("/ongoing-update")
    public MessageResponseDTO<MeetingBookingDto> ongoingUpdate(@RequestBody MyMeetingRequestActionRequest request) {
        return myMeetingRequestService.addOngoingUpdate(request);
    }
}
