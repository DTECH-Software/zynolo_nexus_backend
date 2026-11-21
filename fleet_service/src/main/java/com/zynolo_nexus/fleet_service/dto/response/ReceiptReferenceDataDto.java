package com.zynolo_nexus.fleet_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReceiptReferenceDataDto {
    private List<ReferenceOptionDto> defaultPaymentMethod;
    private List<ReferenceOptionDto> defaultStatus;
    private ReceiptPrivilegesDto privileges;
}
