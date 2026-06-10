package com.zynolo_nexus.meeting_room_booking_service.dto.response;

import com.zynolo_nexus.meeting_room_booking_service.enums.SupportAssignedTeam;
import com.zynolo_nexus.meeting_room_booking_service.enums.SupportServiceCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MeetingBookingSupportServiceDto {
    private Long id;
    private Long serviceId;
    private String serviceCode;
    private String serviceName;
    private SupportServiceCategory serviceCategory;
    private String serviceCategoryDescription;
    private SupportAssignedTeam assignedTeam;
    private String assignedTeamDescription;
    private Boolean chargeable;
    private BigDecimal estimatedAmount;
    private String remarks;
}
