package com.zynolo_nexus.meeting_room_booking_service.service;

import com.zynolo_nexus.meeting_room_booking_service.dto.api.MessageResponseDTO;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.CalendarEventDetailRequest;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.CalendarEventsRequest;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.CalendarReferenceDataRequest;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.CalendarEventDetailDto;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.CalendarEventsResultDto;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.CalendarReferenceDataDto;

public interface CalendarService {
    MessageResponseDTO<CalendarReferenceDataDto> referenceData(CalendarReferenceDataRequest request);
    MessageResponseDTO<CalendarEventsResultDto> events(CalendarEventsRequest request);
    MessageResponseDTO<CalendarEventDetailDto> detail(CalendarEventDetailRequest request);
}
