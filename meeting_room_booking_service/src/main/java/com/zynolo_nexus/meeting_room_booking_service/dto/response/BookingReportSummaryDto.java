package com.zynolo_nexus.meeting_room_booking_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingReportSummaryDto {
    private long totalBookings;
    private long internalMeetings;
    private long externalMeetings;
    private long approvedMeetings;
    private long cancelledMeetings;
    private long pendingApprovals;
}
