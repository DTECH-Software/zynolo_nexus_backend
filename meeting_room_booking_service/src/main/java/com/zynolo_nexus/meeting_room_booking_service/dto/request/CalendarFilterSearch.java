package com.zynolo_nexus.meeting_room_booking_service.dto.request;

import lombok.Data;

import java.time.LocalDate;

@Data
public class CalendarFilterSearch {
    private LocalDate dateFrom;
    private LocalDate dateTo;
    private Long meetingRoomId;
    private String meetingRoomName;
    private String meetingType;
    private String status;
    private String requestedBy;
    private String viewType;
}
