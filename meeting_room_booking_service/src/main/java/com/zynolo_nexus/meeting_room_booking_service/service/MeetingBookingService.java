package com.zynolo_nexus.meeting_room_booking_service.service;

import com.zynolo_nexus.meeting_room_booking_service.dto.api.MessageResponseDTO;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.MeetingBookingFilterRequest;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.MeetingBookingIdRequest;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.MeetingBookingReferenceDataRequest;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.MeetingBookingSaveDraftRequest;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.MeetingBookingUpdateRequest;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.MeetingBookingDto;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.MeetingBookingFilterResultDto;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.MeetingBookingReferenceDataDto;

public interface MeetingBookingService {
    MessageResponseDTO<MeetingBookingReferenceDataDto> referenceData(MeetingBookingReferenceDataRequest request);
    MessageResponseDTO<MeetingBookingDto> saveDraft(MeetingBookingSaveDraftRequest request);
    MessageResponseDTO<MeetingBookingDto> update(MeetingBookingUpdateRequest request);
    MessageResponseDTO<MeetingBookingDto> submit(MeetingBookingIdRequest request);
    MessageResponseDTO<MeetingBookingDto> cancel(MeetingBookingIdRequest request);
    MessageResponseDTO<MeetingBookingDto> view(Long id);
    MessageResponseDTO<MeetingBookingFilterResultDto> filterList(MeetingBookingFilterRequest request);
}
