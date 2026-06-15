package com.zynolo_nexus.meeting_room_booking_service.dto.response;

import com.zynolo_nexus.meeting_room_booking_service.enums.MeetingBookingStatus;
import com.zynolo_nexus.meeting_room_booking_service.enums.MeetingBookingType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CalendarEventDetailDto {
    private Long id;
    private String requestNo;
    private String meetingName;
    private Long meetingRoomId;
    private String meetingRoomCode;
    private String meetingRoomName;
    private LocalDate meetingDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private Integer numberOfAttendees;
    private MeetingBookingType meetingType;
    private String meetingTypeDescription;
    private MeetingBookingStatus status;
    private String statusDescription;
    private String statusColor;
    private boolean tentative;
    private boolean blocksAvailability;
    private String purposeRemarks;
    private List<MeetingBookingRefreshmentDto> refreshments;
    private List<MeetingBookingBeverageDto> beverages;
    private List<MeetingBookingSupportServiceDto> supportServices;
    private CalendarQuickActionsDto actions;
}
