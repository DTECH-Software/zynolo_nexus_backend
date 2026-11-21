package com.zynolo_nexus.fleet_service.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class AssignTripUpdateRequest extends AuditRequest {

    @NotNull(message = "id is required")
    private Long id;

    @NotNull(message = "vehicleId is required")
    private Long vehicleId;

    @NotNull(message = "driverId is required")
    private Long driverId;

    private LocalDateTime scheduledPickupAt;
    private String notes;
}