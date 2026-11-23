package com.zynolo_nexus.meeting_room_booking_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardKpiDto {
    private long totalMeetings;
    private long pendingApprovals;
    private long approvedMeetings;
    private long ongoingMeetings;
    private long completedMeetings;
    private long cancelledMeetings;
}
