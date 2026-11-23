package com.zynolo_nexus.meeting_room_booking_service.service;

import com.zynolo_nexus.meeting_room_booking_service.dto.api.MessageResponseDTO;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.MeetingRefreshmentActiveStatusRequest;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.MeetingRefreshmentCreateRequest;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.MeetingRefreshmentFilterRequest;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.MeetingRefreshmentReferenceDataRequest;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.MeetingRefreshmentUpdateRequest;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.MeetingRefreshmentDto;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.MeetingRefreshmentFilterResultDto;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.MeetingRefreshmentReferenceDataDto;

public interface MeetingRefreshmentService {
    MessageResponseDTO<MeetingRefreshmentDto> create(MeetingRefreshmentCreateRequest request);
    MessageResponseDTO<MeetingRefreshmentDto> update(MeetingRefreshmentUpdateRequest request);
    MessageResponseDTO<MeetingRefreshmentDto> view(Long id);
    MessageResponseDTO<MeetingRefreshmentDto> updateActiveStatus(MeetingRefreshmentActiveStatusRequest request);
    MessageResponseDTO<MeetingRefreshmentFilterResultDto> filterList(MeetingRefreshmentFilterRequest request);
    MessageResponseDTO<MeetingRefreshmentReferenceDataDto> referenceData(MeetingRefreshmentReferenceDataRequest request);
}
