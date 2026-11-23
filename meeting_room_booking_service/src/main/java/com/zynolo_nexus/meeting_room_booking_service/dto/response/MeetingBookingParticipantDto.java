package com.zynolo_nexus.meeting_room_booking_service.dto.response;

import com.zynolo_nexus.meeting_room_booking_service.enums.MeetingParticipantType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MeetingBookingParticipantDto {
    private Long id;
    private MeetingParticipantType participantType;
    private String employeeCode;
    private String employeeName;
    private String name;
    private String company;
    private String contactNo;
    private String email;
}
