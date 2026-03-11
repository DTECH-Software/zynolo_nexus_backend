package com.zynolo_nexus.po_service.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VendorProductViewRequest extends AuditRequest {

    @NotNull(message = "id is required")
    private Long id;
}
