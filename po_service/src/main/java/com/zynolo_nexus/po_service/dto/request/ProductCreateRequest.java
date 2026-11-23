package com.zynolo_nexus.po_service.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class ProductCreateRequest extends AuditRequest {

    @NotBlank(message = "code is required")
    private String code;

    @NotBlank(message = "description is required")
    private String description;

    @NotBlank(message = "uom is required")
    private String uom;

    private BigDecimal defaultPrice;
}
