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
public class DriverReferenceDataDto {
    private List<ReferenceOptionDto> defaultStatus;
    private List<ReferenceOptionDto> defaultAvailability;
    private List<DriverVehicleOptionDto> vehicles;
    private DriverPrivilegesDto privileges;
}