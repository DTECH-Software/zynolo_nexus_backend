package com.zynolo_nexus.po_service.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class PoRequestApproveRequest extends AuditRequest {

    @NotNull(message = "id is required")
    private Long id;

    @NotBlank(message = "vendorCode is required")
    private String vendorCode;

    @Valid
    @NotEmpty(message = "items are required")
    private List<PoRequestApprovalItemRequest> items;

    private String reviewRemark;
}
