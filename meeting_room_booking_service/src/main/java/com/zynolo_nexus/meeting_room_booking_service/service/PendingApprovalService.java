package com.zynolo_nexus.meeting_room_booking_service.service;

import com.zynolo_nexus.meeting_room_booking_service.dto.api.MessageResponseDTO;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.MeetingBookingUpdateRequest;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.PendingApprovalActionRequest;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.PendingApprovalFilterRequest;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.PendingApprovalReferenceDataRequest;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.MeetingBookingDto;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.PendingApprovalFilterResultDto;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.PendingApprovalReferenceDataDto;

public interface PendingApprovalService {
    MessageResponseDTO<PendingApprovalReferenceDataDto> referenceData(PendingApprovalReferenceDataRequest request);
    MessageResponseDTO<PendingApprovalFilterResultDto> filterList(PendingApprovalFilterRequest request);
    MessageResponseDTO<MeetingBookingDto> view(PendingApprovalActionRequest request);
    MessageResponseDTO<MeetingBookingDto> approve(PendingApprovalActionRequest request);
    MessageResponseDTO<MeetingBookingDto> reject(PendingApprovalActionRequest request);
    MessageResponseDTO<MeetingBookingDto> edit(MeetingBookingUpdateRequest request);
}
