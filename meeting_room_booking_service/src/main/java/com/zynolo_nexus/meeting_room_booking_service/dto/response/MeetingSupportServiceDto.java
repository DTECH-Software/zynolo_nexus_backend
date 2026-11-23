package com.zynolo_nexus.meeting_room_booking_service.dto.response;

import com.zynolo_nexus.meeting_room_booking_service.enums.SupportAssignedTeam;
import com.zynolo_nexus.meeting_room_booking_service.enums.SupportServiceCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MeetingSupportServiceDto {
    private Long id;
    private Long companyId;
    private String companyCode;
    private String companyName;
    private String serviceCode;
    private String serviceName;
    private SupportServiceCategory serviceCategory;
    private String serviceCategoryDescription;
    private SupportAssignedTeam assignedTeam;
    private String assignedTeamDescription;
    private Boolean chargeable;
    private BigDecimal defaultCharge;
    private String description;
    private Boolean active;
    private String activeStatusDescription;
    private Boolean selectable;
    private LocalDateTime createdDate;
    private LocalDateTime lastModifiedDate;
    private String createdBy;
    private String lastModifiedBy;
}
