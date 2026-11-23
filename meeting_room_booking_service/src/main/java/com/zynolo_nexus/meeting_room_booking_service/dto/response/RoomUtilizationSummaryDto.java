package com.zynolo_nexus.meeting_room_booking_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomUtilizationSummaryDto {
    private long totalMeetingRooms;
    private long totalBookings;
    private BigDecimal totalBookingHours;
    private BigDecimal averageUtilization;
    private String mostUtilizedRoom;
    private String leastUtilizedRoom;
    private String mostActiveDay;
    private String mostActiveTimeSlot;
    private String highestBookedRoom;
}
