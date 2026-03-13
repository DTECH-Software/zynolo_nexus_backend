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
public class PaymentItemBalanceDto {
    private Long purchaseOrderItemId;
    private String itemCode;
    private String itemDescription;
    private String uom;
    private BigDecimal orderedQuantity;
    private BigDecimal approvedQuantity;
    private BigDecimal receivedQuantity;
    private BigDecimal invoicedQuantity;
    private BigDecimal unitPrice;
    private BigDecimal lineAmount;
}
