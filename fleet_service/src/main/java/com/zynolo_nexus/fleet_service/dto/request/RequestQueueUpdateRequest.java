package com.zynolo_nexus.fleet_service.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RequestQueueUpdateRequest extends AuditRequest {

    @NotNull(message = "id is required")
    private Long id;

    private String dispatcherNotes;
}