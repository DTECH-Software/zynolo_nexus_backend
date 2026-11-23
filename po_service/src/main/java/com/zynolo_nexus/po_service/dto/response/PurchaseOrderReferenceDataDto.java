package com.zynolo_nexus.po_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseOrderReferenceDataDto {
    private List<ReferenceOptionDto> companies;
    private List<ReferenceOptionDto> vendors;
    private List<ReferenceOptionDto> requestTypes;
    private List<ReferenceOptionDto> currencies;
    private List<ReferenceOptionDto> defaultStatus;
    private PurchaseOrderPrivilegesDto privileges;
}