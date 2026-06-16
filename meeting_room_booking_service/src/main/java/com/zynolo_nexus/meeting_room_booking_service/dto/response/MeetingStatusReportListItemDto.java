package com.zynolo_nexus.meeting_room_booking_service.dto.response;

import com.zynolo_nexus.meeting_room_booking_service.enums.MeetingBookingStatus;
import com.zynolo_nexus.meeting_room_booking_service.enums.MeetingBookingType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MeetingStatusReportListItemDto {
    private Long id;
    private String requestNo;
    private String meetingName;
    private String requestedBy;
    private Long meetingRoomId;
    private String meetingRoomName;
    private LocalDate meetingDate;
    private MeetingBookingType meetingType;
    private String meetingTypeDescription;
    private MeetingBookingStatus status;
    private String statusDescription;
    private String approver;
}
