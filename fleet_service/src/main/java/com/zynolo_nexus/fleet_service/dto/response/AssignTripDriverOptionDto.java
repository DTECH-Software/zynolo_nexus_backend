package com.zynolo_nexus.fleet_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssignTripDriverOptionDto {
    private Long id;
    private String name;
    private String mobile;
    private String availability;
    private Long usualVehicleId;
}