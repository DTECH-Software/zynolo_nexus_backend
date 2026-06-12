package com.zynolo_nexus.meeting_room_booking_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardActivityDto {
    private String activityType;
    private String description;
    private String requestNo;
    private String meetingName;
    private String activityBy;
    private LocalDateTime activityDate;
}
