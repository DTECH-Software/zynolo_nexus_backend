package com.zynolo_nexus.meeting_room_booking_service.service;

import com.zynolo_nexus.meeting_room_booking_service.dto.api.MessageResponseDTO;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.CancellationFilterRequest;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.CancellationReferenceDataRequest;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.CancellationSubmitRequest;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.CancellationViewRequest;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.CancellationFilterResultDto;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.CancellationReferenceDataDto;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.MeetingBookingDto;

public interface CancellationService {
    MessageResponseDTO<CancellationReferenceDataDto> referenceData(CancellationReferenceDataRequest request);
    MessageResponseDTO<CancellationFilterResultDto> filterList(CancellationFilterRequest request);
    MessageResponseDTO<MeetingBookingDto> view(CancellationViewRequest request);
    MessageResponseDTO<MeetingBookingDto> cancel(CancellationSubmitRequest request);
}
