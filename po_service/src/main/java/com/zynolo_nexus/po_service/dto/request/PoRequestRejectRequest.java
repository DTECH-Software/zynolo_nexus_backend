package com.zynolo_nexus.po_service.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PoRequestRejectRequest extends AuditRequest {

    @NotNull(message = "id is required")
    private Long id;

    @NotBlank(message = "reviewRemark is required")
    private String reviewRemark;
}
