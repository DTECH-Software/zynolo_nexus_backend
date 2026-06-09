package com.zynolo_nexus.meeting_room_booking_service.dto.request;

import com.zynolo_nexus.meeting_room_booking_service.enums.RoomAvailabilityStatus;
import lombok.Data;

@Data
public class MeetingRoomUpdateRequest {
    private String channel;
    private String ip;
    private String message;
    private String userAgent;
    private String username;
    private Long id;
    private String roomCode;
    private String roomName;
    private Integer capacity;
    private String location;
    private String floor;
    private RoomAvailabilityStatus availabilityStatus;
    private String description;
    private Boolean active;
}
