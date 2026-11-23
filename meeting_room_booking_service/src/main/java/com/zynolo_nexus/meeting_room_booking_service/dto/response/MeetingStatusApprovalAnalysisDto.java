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
public class MeetingStatusApprovalAnalysisDto {
    private long totalSubmitted;
    private long totalApproved;
    private long totalRejected;
    private BigDecimal approvalRate;
    private BigDecimal rejectionRate;
    private String averageApprovalTime;
}
