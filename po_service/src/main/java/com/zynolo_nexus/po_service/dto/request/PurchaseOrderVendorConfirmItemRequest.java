package com.zynolo_nexus.po_service.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class PurchaseOrderVendorConfirmItemRequest {

    @NotBlank(message = "itemCode is required")
    private String itemCode;

    @NotNull(message = "approvedQuantity is required")
    @DecimalMin(value = "0.00", message = "approvedQuantity must be zero or greater")
    private BigDecimal approvedQuantity;
}
