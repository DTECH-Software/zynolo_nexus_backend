package com.zynolo_nexus.meeting_room_booking_service.dto.request;

import lombok.Data;

@Data
public class MeetingSupportServiceFilterSearch {
    private String serviceCode;
    private String serviceName;
    private String serviceCategory;
    private String assignedTeam;
    private Boolean chargeable;
    private Boolean active;
}
