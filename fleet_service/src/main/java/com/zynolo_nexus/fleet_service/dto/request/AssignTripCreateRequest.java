package com.zynolo_nexus.fleet_service.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class AssignTripCreateRequest extends AuditRequest {

    @NotNull(message = "transportRequestId is required")
    private Long transportRequestId;

    @NotNull(message = "vehicleId is required")
    private Long vehicleId;

    @NotNull(message = "driverId is required")
    private Long driverId;

    private LocalDateTime scheduledPickupAt;
    private String notes;
}