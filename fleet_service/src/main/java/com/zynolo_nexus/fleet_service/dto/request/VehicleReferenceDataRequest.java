package com.zynolo_nexus.fleet_service.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VehicleReferenceDataRequest extends AuditRequest {

    /** From login `profileDetails.userRole.code` (auth owns users/roles; fleet does not read `users` table). */
    @NotBlank(message = "roleCode is required")
    private String roleCode;
}
