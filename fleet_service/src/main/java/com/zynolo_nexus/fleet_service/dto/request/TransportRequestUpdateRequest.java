package com.zynolo_nexus.fleet_service.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class TransportRequestUpdateRequest extends AuditRequest {

    @NotNull(message = "id is required")
    private Long id;

    @NotBlank(message = "pickupLocation is required")
    private String pickupLocation;

    @NotBlank(message = "dropLocation is required")
    private String dropLocation;

    @NotNull(message = "requestedPickupAt is required")
    private LocalDateTime requestedPickupAt;

    @NotNull(message = "passengerCount is required")
    private Integer passengerCount;

    private String contactMobile;
    private String purpose;
    private String notes;
}