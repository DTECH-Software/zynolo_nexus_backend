package com.zynolo_nexus.meeting_room_booking_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardOverviewDto {
    private Long companyId;
    private String companyCode;
    private String companyName;
    private LocalDate periodFrom;
    private LocalDate periodTo;
    private String visibilityScope;
    private DashboardKpiDto kpis;
    private List<DashboardRoomUtilizationDto> roomUtilization;
    private DashboardUtilizationSummaryDto utilizationSummary;
    private List<DashboardMeetingListItemDto> todayMeetings;
    private List<DashboardMeetingListItemDto> upcomingMeetings;
    private List<DashboardPendingApprovalDto> pendingApprovals;
    private List<DashboardOngoingMeetingDto> ongoingMeetings;
    private List<DashboardActivityDto> recentActivities;
    private DashboardQuickActionsDto quickActions;
    private DashboardPrivilegesDto privileges;
}
