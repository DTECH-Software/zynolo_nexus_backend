package com.zynolo_nexus.po_service.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PoVendorProductsRequest extends AuditRequest {

    @NotBlank(message = "vendorCode is required")
    private String vendorCode;
}
