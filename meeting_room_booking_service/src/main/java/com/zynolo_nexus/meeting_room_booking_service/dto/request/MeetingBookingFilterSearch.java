package com.zynolo_nexus.meeting_room_booking_service.dto.request;

import lombok.Data;

@Data
public class MeetingBookingFilterSearch {
    private String requestNo;
    private String meetingName;
    private String meetingType;
    private String meetingRoomName;
    private String meetingDate;
    private String status;
    private String createdBy;
}
