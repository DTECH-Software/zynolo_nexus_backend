package com.zynolo_nexus.fleet_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IncidentDto {
    private Long id;
    private Long companyId;
    private Long tripId;
    private String tripNumber;
    private Long driverId;
    private String driverName;
    private String incidentType;
    private String incidentTypeDescription;
    private String severity;
    private String severityDescription;
    private String description;
    private String locationNote;
    private LocalDateTime reportedAt;
    private String status;
    private String statusDescription;
    private LocalDateTime createdDate;
    private LocalDateTime lastModifiedDate;
    private String createdBy;
    private String lastModifiedBy;
}