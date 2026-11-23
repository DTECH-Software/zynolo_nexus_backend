package com.zynolo_nexus.meeting_room_booking_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardQuickActionsDto {
    private boolean createBooking;
    private boolean viewCalendar;
    private boolean myBookings;
    private boolean pendingApprovals;
}
