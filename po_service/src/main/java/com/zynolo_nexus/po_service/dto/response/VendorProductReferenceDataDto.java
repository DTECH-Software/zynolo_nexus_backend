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
public class VendorProductReferenceDataDto {
    private List<ReferenceOptionDto> vendors;
    private List<ReferenceOptionDto> products;
    private List<ReferenceOptionDto> defaultStatus;
    private VendorProductPrivilegesDto privileges;
}
