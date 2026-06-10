package com.zynolo_nexus.meeting_room_booking_service.service;

import com.zynolo_nexus.meeting_room_booking_service.dto.api.MessageResponseDTO;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.MeetingBookingUpdateRequest;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.MyMeetingRequestActionRequest;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.MyMeetingRequestFilterRequest;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.MyMeetingRequestReferenceDataRequest;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.MeetingBookingDto;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.MyMeetingRequestFilterResultDto;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.MyMeetingRequestReferenceDataDto;

public interface MyMeetingRequestService {
    MessageResponseDTO<MyMeetingRequestReferenceDataDto> referenceData(MyMeetingRequestReferenceDataRequest request);
    MessageResponseDTO<MyMeetingRequestFilterResultDto> filterList(MyMeetingRequestFilterRequest request);
    MessageResponseDTO<MeetingBookingDto> view(MyMeetingRequestActionRequest request);
    MessageResponseDTO<MeetingBookingDto> update(MeetingBookingUpdateRequest request);
    MessageResponseDTO<MeetingBookingDto> submit(MyMeetingRequestActionRequest request);
    MessageResponseDTO<MeetingBookingDto> cancel(MyMeetingRequestActionRequest request);
    MessageResponseDTO<MeetingBookingDto> deleteDraft(MyMeetingRequestActionRequest request);
    MessageResponseDTO<MeetingBookingDto> copyAsNew(MyMeetingRequestActionRequest request);
    MessageResponseDTO<MeetingBookingDto> addOngoingUpdate(MyMeetingRequestActionRequest request);
}
