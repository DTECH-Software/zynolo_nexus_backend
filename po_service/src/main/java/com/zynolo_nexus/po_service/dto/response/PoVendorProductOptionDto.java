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
public class PoVendorProductOptionDto {
    private String code;
    private String description;
    private String uom;
    private String vendorProductCode;
    private BigDecimal defaultPrice;
    private BigDecimal lastPrice;
    private Integer leadTimeDays;
}
