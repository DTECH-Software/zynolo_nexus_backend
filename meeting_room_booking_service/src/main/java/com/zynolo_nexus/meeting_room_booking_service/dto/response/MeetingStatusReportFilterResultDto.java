package com.zynolo_nexus.meeting_room_booking_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MeetingStatusReportFilterResultDto {
    private MeetingStatusReportSummaryDto summary;
    private List<MeetingStatusSummaryItemDto> statusSummary;
    private MeetingStatusApprovalAnalysisDto approvalAnalysis;
    private List<MeetingStatusApproverPerformanceDto> approverPerformance;
    private List<MeetingStatusCancellationAnalysisDto> cancellationAnalysis;
    private List<MeetingStatusMonthlyTrendDto> monthlyStatusTrend;
    private List<MeetingStatusReportListItemDto> content;
    private int size;
    private long totalRecords;
    private int page;
    private int totalPages;
}
