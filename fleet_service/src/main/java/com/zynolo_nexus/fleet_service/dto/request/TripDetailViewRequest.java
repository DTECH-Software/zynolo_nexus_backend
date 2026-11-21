package com.zynolo_nexus.fleet_service.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TripDetailViewRequest extends AuditRequest {

    @NotNull(message = "id is required")
    private Long id;

    /** Max trail points to return (default 50, max 100). */
    private Integer trailLimit;
}
