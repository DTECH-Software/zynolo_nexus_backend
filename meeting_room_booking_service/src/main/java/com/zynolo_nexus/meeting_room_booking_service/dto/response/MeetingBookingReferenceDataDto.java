package com.zynolo_nexus.meeting_room_booking_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MeetingBookingReferenceDataDto {
    private Long companyId;
    private String companyCode;
    private String companyName;
    private List<ReferenceOptionDto> meetingTypes;
    private List<ReferenceOptionDto> statuses;
    private BigDecimal externalMeetingRoomHourlyRate;
    private List<MeetingRoomDto> meetingRooms;
    private List<MeetingVendorDto> activeVendors;
    private List<MeetingRefreshmentDto> refreshments;
    private List<MeetingBeverageDto> beverages;
    private List<MeetingSupportServiceDto> supportServices;
    private MeetingBookingPrivilegesDto privileges;
}
