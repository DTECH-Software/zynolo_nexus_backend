package com.zynolo_nexus.po_service.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class InvoiceReceiptReceiveItemRequest {

    @NotBlank(message = "itemCode is required")
    private String itemCode;

    @NotNull(message = "invoicedQuantity is required")
    @DecimalMin(value = "0.01", message = "invoicedQuantity must be greater than zero")
    private BigDecimal invoicedQuantity;

    @NotNull(message = "unitPrice is required")
    @DecimalMin(value = "0.00", inclusive = false, message = "unitPrice must be greater than zero")
    private BigDecimal unitPrice;
}
