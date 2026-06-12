package com.zynolo_nexus.meeting_room_booking_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardOngoingMeetingDto {
    private Long id;
    private String requestNo;
    private String meetingName;
    private String meetingRoomName;
    private LocalTime startedAt;
    private BigDecimal durationHours;
    private DashboardOngoingActionsDto actions;
}
