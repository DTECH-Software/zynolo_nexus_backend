package com.zynolo_nexus.fleet_service.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VehicleCreateRequest extends AuditRequest {

    @NotBlank(message = "registration is required")
    private String registration;

    @NotBlank(message = "vehicleType is required")
    private String vehicleType;

    @NotNull(message = "passengerCapacity is required")
    @Min(value = 1, message = "passengerCapacity must be at least 1")
    private Integer passengerCapacity;

    private String notes;
}
