package com.zynolo_nexus.meeting_room_booking_service.dto.request;

import lombok.Data;

import java.time.LocalDate;

@Data
public class MeetingStatusReportFilterSearch {
    private LocalDate dateFrom;
    private LocalDate dateTo;
    private String status;
    private Long meetingRoomId;
    private String meetingRoomName;
    private String meetingType;
    private String requestedBy;
    private String department;
    private String approver;
    private String requestNo;
}
