package com.zynolo_nexus.meeting_room_booking_service.dto.request;

import lombok.Data;

@Data
public class MeetingBookingSupportServiceRequest {
    private Long serviceId;
    private String remarks;
}
