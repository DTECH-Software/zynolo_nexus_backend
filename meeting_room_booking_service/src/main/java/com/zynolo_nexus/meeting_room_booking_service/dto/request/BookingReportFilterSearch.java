package com.zynolo_nexus.meeting_room_booking_service.dto.request;

import lombok.Data;

import java.time.LocalDate;

@Data
public class BookingReportFilterSearch {
    private LocalDate dateFrom;
    private LocalDate dateTo;
    private String requestNo;
    private String meetingName;
    private Long meetingRoomId;
    private String meetingRoomName;
    private String meetingType;
    private String status;
    private String requestedBy;
    private String department;
    private String approver;
    private Integer attendeeCountMin;
    private Integer attendeeCountMax;
}
