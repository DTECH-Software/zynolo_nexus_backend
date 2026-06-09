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
public class MeetingRoomReferenceDataDto {
    private Long companyId;
    private String companyCode;
    private String companyName;
    private List<ReferenceOptionDto> availabilityStatuses;
    private Integer maxRoomCount;
    private Long currentRoomCount;
}
