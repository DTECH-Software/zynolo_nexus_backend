package com.zynolo_nexus.meeting_room_booking_service.dto.request;

import com.zynolo_nexus.meeting_room_booking_service.enums.MeetingBookingType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
public class MeetingBookingSaveDraftRequest {
    private String channel;
    private String ip;
    private String message;
    private String userAgent;
    private String username;

    @NotBlank(message = "Meeting name is required")
    private String meetingName;

    @NotNull(message = "Meeting type is required")
    private MeetingBookingType meetingType;

    private Long customerId;
    private String contactPerson;
    private String contactNumber;

    @NotNull(message = "Meeting room is required")
    private Long meetingRoomId;

    @NotNull(message = "Meeting date is required")
    private LocalDate meetingDate;

    @NotNull(message = "Start time is required")
    private LocalTime startTime;

    @NotNull(message = "End time is required")
    private LocalTime endTime;

    @NotNull(message = "Number of attendees is required")
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
