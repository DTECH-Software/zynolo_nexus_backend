package com.zynolo_nexus.meeting_room_booking_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardPendingApprovalDto {
    private Long id;
    private String requestNo;
    private String meetingName;
    private String requestedBy;
    private LocalDate meetingDate;
    private String meetingRoomName;
    private DashboardApprovalActionsDto actions;
}
