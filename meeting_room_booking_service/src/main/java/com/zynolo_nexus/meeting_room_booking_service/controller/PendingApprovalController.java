package com.zynolo_nexus.meeting_room_booking_service.controller;

import com.zynolo_nexus.meeting_room_booking_service.dto.api.MessageResponseDTO;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.MeetingBookingUpdateRequest;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.PendingApprovalActionRequest;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.PendingApprovalFilterRequest;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.PendingApprovalReferenceDataRequest;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.MeetingBookingDto;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.PendingApprovalFilterResultDto;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.PendingApprovalReferenceDataDto;
import com.zynolo_nexus.meeting_room_booking_service.service.PendingApprovalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/meeting-room/pending-approvals")
@RequiredArgsConstructor
public class PendingApprovalController {

    private final PendingApprovalService pendingApprovalService;

    @PostMapping("/reference-data")
    public MessageResponseDTO<PendingApprovalReferenceDataDto> referenceData(
            @RequestBody(required = false) PendingApprovalReferenceDataRequest request) {
        return pendingApprovalService.referenceData(request);
    }

    @PostMapping("/filter-list")
    public MessageResponseDTO<PendingApprovalFilterResultDto> filterList(@RequestBody PendingApprovalFilterRequest request) {
        return pendingApprovalService.filterList(request);
    }

    @PostMapping("/view")
    public MessageResponseDTO<MeetingBookingDto> view(@RequestBody PendingApprovalActionRequest request) {
        return pendingApprovalService.view(request);
    }

    @PostMapping("/approve")
    public MessageResponseDTO<MeetingBookingDto> approve(@RequestBody PendingApprovalActionRequest request) {
        return pendingApprovalService.approve(request);
    }

    @PostMapping("/reject")
    public MessageResponseDTO<MeetingBookingDto> reject(@RequestBody PendingApprovalActionRequest request) {
        return pendingApprovalService.reject(request);
    }

    @PostMapping("/edit")
    public MessageResponseDTO<MeetingBookingDto> edit(@Valid @RequestBody MeetingBookingUpdateRequest request) {
        return pendingApprovalService.edit(request);
    }
}
