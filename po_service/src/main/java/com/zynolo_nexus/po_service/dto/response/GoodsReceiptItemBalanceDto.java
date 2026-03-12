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
public class GoodsReceiptItemBalanceDto {
    private Long purchaseOrderItemId;
    private String itemCode;
    private String itemDescription;
    private String uom;
    private BigDecimal orderedQuantity;
    private BigDecimal receivedQuantity;
    private BigDecimal balanceQuantity;
    private BigDecimal unitPrice;
    private BigDecimal lineAmount;
}
