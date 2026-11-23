package com.zynolo_nexus.meeting_room_booking_service.dto.response;

import com.zynolo_nexus.meeting_room_booking_service.enums.MeetingBookingStatus;
import com.zynolo_nexus.meeting_room_booking_service.enums.MeetingBookingType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PendingApprovalListItemDto {
    private Long id;
    private String requestNo;
    private String requestedBy;
    private String submittedBy;
    private LocalDateTime submittedDate;
    private String meetingName;
    private MeetingBookingType meetingType;
    private String meetingTypeDescription;
    private Long meetingRoomId;
    private String meetingRoomName;
    private LocalDate meetingDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private Integer numberOfAttendees;
    private MeetingBookingStatus status;
    private String statusDescription;
    private PendingApprovalActionsDto actions;
}
