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
public class TodayScheduleReferenceDataDto {
    private List<ReferenceOptionDto> defaultTripStatus;
    private List<DriverVehicleOptionDto> vehicles;
    private List<AssignTripDriverOptionDto> drivers;
    private TodaySchedulePrivilegesDto privileges;
}