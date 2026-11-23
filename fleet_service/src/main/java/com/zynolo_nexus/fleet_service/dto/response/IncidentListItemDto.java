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
public class IncidentListItemDto {
    private Long id;
    private Long tripId;
    private String tripNumber;
    private String incidentType;
    private String incidentTypeDescription;
    private String severity;
    private String severityDescription;
    private String status;
    private String statusDescription;
    private LocalDateTime reportedAt;
    private LocalDateTime createdDate;
    private String createdBy;
}