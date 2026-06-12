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
public class DashboardUtilizationSummaryDto {
    private BigDecimal totalBookingHours;
    private BigDecimal averageMeetingDuration;
    private String mostUsedRoom;
    private String leastUsedRoom;
}
