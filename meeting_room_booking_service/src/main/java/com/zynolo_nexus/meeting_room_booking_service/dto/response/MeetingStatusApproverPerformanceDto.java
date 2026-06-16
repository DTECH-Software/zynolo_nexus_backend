package com.zynolo_nexus.meeting_room_booking_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MeetingStatusApproverPerformanceDto {
    private String approver;
    private long requests;
    private long approved;
    private long rejected;
    private String averageApprovalTime;
}
