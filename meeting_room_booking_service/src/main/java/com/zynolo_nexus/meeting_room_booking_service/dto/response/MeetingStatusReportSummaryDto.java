package com.zynolo_nexus.meeting_room_booking_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MeetingStatusReportSummaryDto {
    private long totalRequests;
    private long pendingApprovals;
    private long approved;
    private long rejected;
    private long ongoing;
    private long completed;
    private long cancelled;
}
