package com.zynolo_nexus.po_service.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DepartmentStatusRequest extends AuditRequest {

    @NotNull(message = "id is required")
    private Long id;

    @NotBlank(message = "status is required")
    private String status;
}
