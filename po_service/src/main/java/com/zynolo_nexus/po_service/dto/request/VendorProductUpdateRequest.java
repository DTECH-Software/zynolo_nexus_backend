package com.zynolo_nexus.po_service.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class VendorProductUpdateRequest extends AuditRequest {

    @NotNull(message = "id is required")
    private Long id;

    @NotBlank(message = "vendorCode is required")
    private String vendorCode;

    @NotBlank(message = "productCode is required")
    private String productCode;

    private String vendorProductCode;
    private BigDecimal lastPrice;
    private Integer leadTimeDays;
    private String status;
}
