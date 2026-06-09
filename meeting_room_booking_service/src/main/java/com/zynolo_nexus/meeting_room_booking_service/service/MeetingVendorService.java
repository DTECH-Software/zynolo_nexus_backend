package com.zynolo_nexus.meeting_room_booking_service.service;

import com.zynolo_nexus.meeting_room_booking_service.dto.api.MessageResponseDTO;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.MeetingVendorActiveStatusRequest;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.MeetingVendorCreateRequest;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.MeetingVendorFilterRequest;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.MeetingVendorReferenceDataRequest;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.MeetingVendorUpdateRequest;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.MeetingVendorDto;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.MeetingVendorFilterResultDto;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.MeetingVendorReferenceDataDto;

public interface MeetingVendorService {
    MessageResponseDTO<MeetingVendorDto> create(MeetingVendorCreateRequest request);
    MessageResponseDTO<MeetingVendorDto> update(MeetingVendorUpdateRequest request);
    MessageResponseDTO<MeetingVendorDto> view(Long id);
    MessageResponseDTO<MeetingVendorDto> updateActiveStatus(MeetingVendorActiveStatusRequest request);
    MessageResponseDTO<MeetingVendorFilterResultDto> filterList(MeetingVendorFilterRequest request);
    MessageResponseDTO<MeetingVendorReferenceDataDto> referenceData(MeetingVendorReferenceDataRequest request);
}
