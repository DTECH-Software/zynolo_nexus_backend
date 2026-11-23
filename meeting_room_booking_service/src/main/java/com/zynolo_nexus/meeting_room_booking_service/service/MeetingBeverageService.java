package com.zynolo_nexus.meeting_room_booking_service.service;

import com.zynolo_nexus.meeting_room_booking_service.dto.api.MessageResponseDTO;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.MeetingBeverageActiveStatusRequest;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.MeetingBeverageCreateRequest;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.MeetingBeverageFilterRequest;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.MeetingBeverageReferenceDataRequest;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.MeetingBeverageUpdateRequest;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.MeetingBeverageDto;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.MeetingBeverageFilterResultDto;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.MeetingBeverageReferenceDataDto;

public interface MeetingBeverageService {
    MessageResponseDTO<MeetingBeverageDto> create(MeetingBeverageCreateRequest request);
    MessageResponseDTO<MeetingBeverageDto> update(MeetingBeverageUpdateRequest request);
    MessageResponseDTO<MeetingBeverageDto> view(Long id);
    MessageResponseDTO<MeetingBeverageDto> updateActiveStatus(MeetingBeverageActiveStatusRequest request);
    MessageResponseDTO<MeetingBeverageFilterResultDto> filterList(MeetingBeverageFilterRequest request);
    MessageResponseDTO<MeetingBeverageReferenceDataDto> referenceData(MeetingBeverageReferenceDataRequest request);
}
