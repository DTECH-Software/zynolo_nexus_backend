package com.zynolo_nexus.meeting_room_booking_service.dto.request;

import lombok.Data;

@Data
public class MeetingRoomFilterSearch {
    private String roomCode;
    private String roomName;
    private String location;
    private String floor;
    private String availabilityStatus;
    private Boolean active;
    private String status;
}

