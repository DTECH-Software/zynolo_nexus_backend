package com.zynolo_nexus.meeting_room_booking_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CalendarReferenceDataDto {
    private Long companyId;
    private String companyCode;
    private String companyName;
    private List<ReferenceOptionDto> viewTypes;
    private List<ReferenceOptionDto> meetingTypes;
    private List<ReferenceOptionDto> statuses;
    private List<ReferenceOptionDto> statusColors;
    private List<MeetingRoomDto> meetingRooms;
    private CalendarPrivilegesDto privileges;
    private CalendarQuickActionsDto quickActions;
}
