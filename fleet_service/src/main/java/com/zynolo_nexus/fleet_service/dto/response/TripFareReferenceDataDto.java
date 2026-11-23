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
public class TripFareReferenceDataDto {
    private List<ReferenceOptionDto> defaultPaymentMethod;
    private List<ReferenceOptionDto> defaultCurrency;
    private List<ReferenceOptionDto> defaultStatus;
    private String defaultCurrencyCode;
    private TripFarePrivilegesDto privileges;
}
