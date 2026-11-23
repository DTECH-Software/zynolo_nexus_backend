package com.zynolo_nexus.fleet_service.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OutstandingViewRequest extends AuditRequest {

    @NotBlank(message = "itemType is required")
    private String itemType;

    @NotNull(message = "id is required")
    private Long id;
}
