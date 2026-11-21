package com.zynolo_nexus.fleet_service.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class IncidentFilterSearch {
    private Long tripId;
    private String incidentType;
    private String severity;
    private String status;
}