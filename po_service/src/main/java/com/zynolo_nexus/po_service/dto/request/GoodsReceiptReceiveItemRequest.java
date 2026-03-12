package com.zynolo_nexus.po_service.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class GoodsReceiptReceiveItemRequest {

    @NotBlank(message = "itemCode is required")
    private String itemCode;

    @NotNull(message = "receivedQuantity is required")
    @DecimalMin(value = "0.01", message = "receivedQuantity must be greater than zero")
    private BigDecimal receivedQuantity;
}
