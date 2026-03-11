package com.zynolo_nexus.po_service.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class VendorProductCreateRequest extends AuditRequest {

    @NotBlank(message = "vendorCode is required")
    private String vendorCode;

    @NotBlank(message = "productCode is required")
    private String productCode;

    private String vendorProductCode;
    private BigDecimal lastPrice;
    private Integer leadTimeDays;
}
