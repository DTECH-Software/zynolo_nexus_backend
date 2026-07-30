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
public class MeetingBeverageReferenceDataDto {
    private Long companyId;
    private String companyCode;
    private String companyName;
    private List<ReferenceOptionDto> commonBeverages;
    private List<ReferenceOptionDto> statuses;
    private List<MeetingVendorDto> activeVendors;
    private MeetingBeveragePrivilegesDto privileges;
}
