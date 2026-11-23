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
public class TripStatusReferenceDataDto {
    private Long driverId;
    private String driverName;
    private List<ReferenceOptionDto> defaultTripStatus;
    private List<ReferenceOptionDto> allowedTransitions;
    private TripStatusPrivilegesDto privileges;
}