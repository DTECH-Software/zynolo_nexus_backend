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
public class DashboardRoomUtilizationDto {
    private Long roomId;
    private String roomCode;
    private String roomName;
    private long bookingCount;
    private BigDecimal totalBookingHours;
    private BigDecimal periodCapacityHours;
    private BigDecimal utilizationPercentage;
}
