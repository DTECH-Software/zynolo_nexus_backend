package com.zynolo_nexus.meeting_room_booking_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CalendarRoomAvailabilityDto {
    private Long roomId;
    private String roomCode;
    private String roomName;
    private LocalDate date;
    private long blockingBookingCount;
    private boolean available;
}
