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
public class BookingReportDetailDto {
    private Long id;
    private String requestNo;
    private String meetingName;
    private MeetingBookingType meetingType;
    private String meetingTypeDescription;
    private Long meetingRoomId;
    private String meetingRoomCode;
    private String meetingRoomName;
    private LocalDate meetingDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private Integer attendees;
    private String remarks;
    private MeetingBookingStatus status;
    private String statusDescription;
    private List<MeetingBookingRefreshmentDto> refreshments;
    private List<MeetingBookingBeverageDto> beverages;
    private List<MeetingBookingSupportServiceDto> supportServices;
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
    private LocalDateTime createdDate;
    private String createdBy;
}
