package com.zynolo_nexus.fleet_service.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class IncidentCreateRequest extends AuditRequest {

    @NotNull(message = "tripId is required")
    private Long tripId;

    @NotBlank(message = "incidentType is required")
    private String incidentType;

    @NotBlank(message = "severity is required")
    private String severity;

    @NotBlank(message = "description is required")
    private String description;

    private String locationNote;
}