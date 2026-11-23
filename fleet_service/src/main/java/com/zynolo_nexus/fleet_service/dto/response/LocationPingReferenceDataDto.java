package com.zynolo_nexus.fleet_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LocationPingReferenceDataDto {
    private Long driverId;
    private String driverName;
    private Integer recommendedPingSeconds;
    private LocationPingPrivilegesDto privileges;
}