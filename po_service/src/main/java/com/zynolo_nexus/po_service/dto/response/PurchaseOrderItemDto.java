package com.zynolo_nexus.po_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseOrderItemDto {
    private Long id;
    private String itemCode;
    private String itemDescription;
    private String uom;
    private BigDecimal quantity;
    private BigDecimal approvedQuantity;
    private BigDecimal unitPrice;
    private BigDecimal lineAmount;
}
