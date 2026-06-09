package com.zynolo_nexus.meeting_room_booking_service.dto.request;

import lombok.Data;

@Data
public class MeetingSupportServiceActiveStatusRequest {
    private String channel;
    private String ip;
    private String message;
    private String userAgent;
    private String username;
    private Long id;
    private Boolean active;
}
