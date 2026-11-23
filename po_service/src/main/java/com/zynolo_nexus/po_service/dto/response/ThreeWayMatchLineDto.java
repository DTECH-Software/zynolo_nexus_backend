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
public class ThreeWayMatchLineDto {
    private Long purchaseOrderItemId;
    private String itemCode;
    private String itemDescription;
    private String uom;
    private BigDecimal orderedQuantity;
    private BigDecimal approvedQuantity;
    private BigDecimal receivedQuantity;
    private BigDecimal invoicedQuantity;
    private BigDecimal poUnitPrice;
    private BigDecimal invoiceUnitPrice;
    private BigDecimal poLineAmount;
    private BigDecimal invoiceLineAmount;
    private String lineMatchStatus;
    private String lineMatchStatusDescription;
}
