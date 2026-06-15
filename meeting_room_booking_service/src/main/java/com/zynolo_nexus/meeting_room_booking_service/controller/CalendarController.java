package com.zynolo_nexus.meeting_room_booking_service.controller;

import com.zynolo_nexus.meeting_room_booking_service.dto.api.MessageResponseDTO;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.CalendarEventDetailRequest;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.CalendarEventsRequest;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.CalendarReferenceDataRequest;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.CalendarEventDetailDto;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.CalendarEventsResultDto;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.CalendarReferenceDataDto;
import com.zynolo_nexus.meeting_room_booking_service.service.CalendarService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/meeting-room/calendar")
@RequiredArgsConstructor
public class CalendarController {

    private final CalendarService calendarService;

    @PostMapping("/reference-data")
    public MessageResponseDTO<CalendarReferenceDataDto> referenceData(@RequestBody(required = false) CalendarReferenceDataRequest request) {
        return calendarService.referenceData(request);
    }

    @PostMapping("/events")
    public MessageResponseDTO<CalendarEventsResultDto> events(@RequestBody CalendarEventsRequest request) {
        return calendarService.events(request);
    }

    @PostMapping("/detail")
    public MessageResponseDTO<CalendarEventDetailDto> detail(@RequestBody CalendarEventDetailRequest request) {
        return calendarService.detail(request);
    }
}
