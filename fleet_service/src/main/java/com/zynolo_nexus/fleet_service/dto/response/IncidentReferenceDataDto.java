package com.zynolo_nexus.fleet_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IncidentReferenceDataDto {
    private Long driverId;
    private String driverName;
    private List<ReferenceOptionDto> defaultIncidentType;
    private List<ReferenceOptionDto> defaultSeverity;
    private List<ReferenceOptionDto> defaultStatus;
    private IncidentPrivilegesDto privileges;
}