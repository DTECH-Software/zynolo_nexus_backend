package com.zynolo_nexus.meeting_room_booking_service.service;

import com.zynolo_nexus.meeting_room_booking_service.dto.api.MessageResponseDTO;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.OngoingUpdateFilterRequest;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.OngoingUpdateReferenceDataRequest;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.OngoingUpdateSaveRequest;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.OngoingUpdateViewRequest;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.MeetingBookingDto;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.OngoingUpdateFilterResultDto;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.OngoingUpdateReferenceDataDto;

public interface OngoingUpdateService {
    MessageResponseDTO<OngoingUpdateReferenceDataDto> referenceData(OngoingUpdateReferenceDataRequest request);
    MessageResponseDTO<OngoingUpdateFilterResultDto> filterList(OngoingUpdateFilterRequest request);
    MessageResponseDTO<MeetingBookingDto> view(OngoingUpdateViewRequest request);
    MessageResponseDTO<MeetingBookingDto> saveUpdate(OngoingUpdateSaveRequest request);
}
