package com.zynolo_nexus.fleet_service.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DriverCreateRequest extends AuditRequest {

    @NotBlank(message = "name is required")
    private String name;

    @NotBlank(message = "mobile is required")
    private String mobile;

    private String licenceNumber;
    private String linkedUsername;
    private Long usualVehicleId;
    private String availability;
    private String notes;
}