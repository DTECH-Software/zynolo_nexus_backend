package com.zynolo_nexus.po_service.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DepartmentCreateRequest extends AuditRequest {

    @NotBlank(message = "code is required")
    private String code;

    @NotBlank(message = "description is required")
    private String description;
}
