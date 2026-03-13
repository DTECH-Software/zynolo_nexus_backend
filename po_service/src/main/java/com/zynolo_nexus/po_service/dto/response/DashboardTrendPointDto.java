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
public class DashboardTrendPointDto {
    private String period;
    private long requestCount;
    private BigDecimal requestAmount;
    private long purchaseOrderCount;
    private BigDecimal purchaseOrderAmount;
    private BigDecimal invoicedAmount;
    private BigDecimal paidAmount;
}
