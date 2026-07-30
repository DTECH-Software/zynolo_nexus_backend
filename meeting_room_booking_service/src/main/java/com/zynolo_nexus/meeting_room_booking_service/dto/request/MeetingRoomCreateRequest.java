package com.zynolo_nexus.meeting_room_booking_service.dto.request;

import com.zynolo_nexus.meeting_room_booking_service.enums.RoomAvailabilityStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class MeetingRoomCreateRequest {
    private String channel;
    private String ip;
    private String message;
    private String userAgent;
    private String username;

    @NotBlank(message = "Room code is required")
    private String roomCode;

    @NotBlank(message = "Room name is required")
    private String roomName;

    @NotNull(message = "Capacity is required")
    @Min(value = 1, message = "Capacity must be greater than 0")
    private Integer capacity;

    private String location;
    private String floor;

    @DecimalMin(value = "0.00", message = "Per-hour charge cannot be negative")
    private BigDecimal perHourCharge;

    @NotNull(message = "Availability status is required")
    private RoomAvailabilityStatus availabilityStatus;

    private String description;

    @NotNull(message = "Active status is required")
    private Boolean active;
}
