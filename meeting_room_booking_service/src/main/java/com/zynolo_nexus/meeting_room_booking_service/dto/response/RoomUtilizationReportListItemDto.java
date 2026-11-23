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
public class RoomUtilizationReportListItemDto {
    private Long roomId;
    private String roomCode;
    private String roomName;
    private Integer capacity;
    private long totalBookings;
    private BigDecimal totalHours;
    private BigDecimal availableHours;
    private BigDecimal utilizationPercentage;
    private long internalMeetings;
    private long externalMeetings;
}
