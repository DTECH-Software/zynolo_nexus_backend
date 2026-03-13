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
public class TrackingReferenceDataDto {
    private List<ReferenceOptionDto> companies;
    private List<ReferenceOptionDto> vendors;
    private List<ReferenceOptionDto> poStatuses;
    private List<ReferenceOptionDto> matchStatuses;
    private List<ReferenceOptionDto> paymentStatuses;
    private TrackingPrivilegesDto privileges;
}
