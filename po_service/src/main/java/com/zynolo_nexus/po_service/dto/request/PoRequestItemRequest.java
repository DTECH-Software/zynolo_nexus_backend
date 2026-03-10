package com.zynolo_nexus.po_service.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class PoRequestItemRequest {

    private String itemCode;

    @NotBlank(message = "itemDescription is required")
    private String itemDescription;

    private String uom;

    @NotNull(message = "quantity is required")
    @DecimalMin(value = "0.01", message = "quantity must be greater than zero")
    private BigDecimal quantity;

    @NotNull(message = "unitPrice is required")
    @DecimalMin(value = "0.00", inclusive = false, message = "unitPrice must be greater than zero")
    private BigDecimal unitPrice;
}
