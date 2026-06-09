package com.zynolo_nexus.meeting_room_booking_service.service;

import com.zynolo_nexus.meeting_room_booking_service.dto.api.MessageResponseDTO;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.MeetingRoomActiveStatusRequest;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.MeetingRoomCreateRequest;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.MeetingRoomFilterRequest;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.MeetingRoomReferenceDataRequest;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.MeetingRoomUpdateRequest;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.MeetingRoomDto;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.MeetingRoomFilterResultDto;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.MeetingRoomReferenceDataDto;

public interface MeetingRoomService {
    MessageResponseDTO<MeetingRoomDto> create(MeetingRoomCreateRequest request);
    MessageResponseDTO<MeetingRoomDto> update(MeetingRoomUpdateRequest request);
    MessageResponseDTO<MeetingRoomDto> view(Long id);
    MessageResponseDTO<MeetingRoomDto> updateActiveStatus(MeetingRoomActiveStatusRequest request);
    MessageResponseDTO<MeetingRoomFilterResultDto> filterList(MeetingRoomFilterRequest request);
    MessageResponseDTO<MeetingRoomReferenceDataDto> referenceData(MeetingRoomReferenceDataRequest request);
}
