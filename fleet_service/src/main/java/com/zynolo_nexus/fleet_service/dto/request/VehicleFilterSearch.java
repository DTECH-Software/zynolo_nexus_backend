package com.zynolo_nexus.fleet_service.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VehicleFilterSearch {
    private String registration;
    private String vehicleType;
    private String status;
}
