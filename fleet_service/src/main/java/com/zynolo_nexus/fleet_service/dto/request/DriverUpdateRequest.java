package com.zynolo_nexus.fleet_service.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DriverUpdateRequest extends AuditRequest {

    @NotNull(message = "id is required")
    private Long id;

    @NotBlank(message = "name is required")
    private String name;

    @NotBlank(message = "mobile is required")
    private String mobile;

    private String licenceNumber;
    private String linkedUsername;
    private Long usualVehicleId;
    private String availability;
    private String notes;
    private String status;
}