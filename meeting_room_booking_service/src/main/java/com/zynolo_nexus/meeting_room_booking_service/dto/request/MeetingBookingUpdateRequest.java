package com.zynolo_nexus.meeting_room_booking_service.dto.request;

import com.zynolo_nexus.meeting_room_booking_service.enums.MeetingBookingType;
import jakarta.validation.Valid;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
public class MeetingBookingUpdateRequest {
    private String channel;
    private String ip;
    private String message;
    private String userAgent;
    private String username;
    private Long id;
    private String meetingName;
    private MeetingBookingType meetingType;
    private Long customerId;
    private String customerCode;
    private String customerCompanyName;
    private String contactPerson;
    private String contactNumber;
    private Long meetingRoomId;
    private LocalDate meetingDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private Integer numberOfAttendees;
    private String purposeRemarks;

    @Valid
    private List<MeetingBookingRefreshmentRequest> refreshments;

    @Valid
    private List<MeetingBookingBeverageRequest> beverages;

    @Valid
    private List<MeetingBookingSupportServiceRequest> supportServices;

    @Valid
    private List<MeetingBookingParticipantRequest> participants;
}
