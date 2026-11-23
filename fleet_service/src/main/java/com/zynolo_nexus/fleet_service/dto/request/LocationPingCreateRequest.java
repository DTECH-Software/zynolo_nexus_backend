package com.zynolo_nexus.fleet_service.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class LocationPingCreateRequest extends AuditRequest {

    @NotNull(message = "tripId is required")
    private Long tripId;

    @NotNull(message = "latitude is required")
    private Double latitude;

    @NotNull(message = "longitude is required")
    private Double longitude;

    private Double accuracyMeters;
    private Double speedKmh;
    private Double headingDegrees;
    private LocalDateTime recordedAt;
}