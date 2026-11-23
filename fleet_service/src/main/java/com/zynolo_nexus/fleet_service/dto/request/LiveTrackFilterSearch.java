package com.zynolo_nexus.fleet_service.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LiveTrackFilterSearch {
    private String tripNumber;
    private String status;
    private Long driverId;
    private Long vehicleId;
}