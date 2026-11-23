package com.zynolo_nexus.meeting_room_booking_service.dto.request;

import com.zynolo_nexus.meeting_room_booking_service.enums.MeetingParticipantType;
import jakarta.validation.constraints.Email;
import lombok.Data;

@Data
public class MeetingBookingParticipantRequest {
    private MeetingParticipantType participantType;
    private String employeeCode;
    private String employeeName;
    private String name;
    private String company;
    private String contactNo;

    @Email(message = "Invalid participant email")
    private String email;
}
