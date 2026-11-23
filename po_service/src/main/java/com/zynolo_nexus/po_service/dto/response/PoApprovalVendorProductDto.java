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
public class PoApprovalVendorProductDto {
    private Long requestItemId;
    private String itemCode;
    private String itemDescription;
    private String uom;
    private BigDecimal quantity;
    private BigDecimal estimatedUnitPrice;
    private boolean available;
    private String vendorProductCode;
    private BigDecimal defaultPrice;
    private BigDecimal lastPrice;
    private Integer leadTimeDays;
}
