package com.zynolo_nexus.meeting_room_booking_service.dto.request;

import lombok.Data;

@Data
public class MeetingBeverageFilterRequest {
    private String channel;
    private String ip;
    private String message;
    private String userAgent;
    private String username;
    private Integer page;
    private Integer size;
    private String sortColumn;
    private String sortDirection;
    private MeetingBeverageFilterSearch search;
}
