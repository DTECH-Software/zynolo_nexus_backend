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
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MeetingStatusReportDetailDto {
    private Long id;
    private String requestNo;
    private String meetingName;
    private MeetingBookingType meetingType;
    private String meetingTypeDescription;
    private Long meetingRoomId;
    private String meetingRoomName;
    private LocalDate meetingDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private Integer attendees;
    private MeetingBookingStatus status;
    private String statusDescription;
    private String requestedBy;
    private String submittedBy;
    private LocalDateTime submittedDate;
    private String approvedBy;
    private LocalDateTime approvedDate;
    private String approvalRemark;
    private String rejectedBy;
    private LocalDateTime rejectedDate;
    private String rejectionRemark;
    private String cancelledBy;
    private LocalDateTime cancelledDate;
    private String cancellationReason;
    private List<MeetingStatusHistoryDto> statusHistory;
    private List<MeetingBookingRefreshmentDto> refreshments;
    private List<MeetingBookingBeverageDto> beverages;
    private List<MeetingBookingSupportServiceDto> supportServices;
}
