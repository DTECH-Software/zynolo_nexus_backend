package com.zynolo_nexus.fleet_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LiveTrackListItemDto {
    private Long tripId;
    private String tripNumber;
    private String status;
    private String statusDescription;
    private Long driverId;
    private String driverName;
    private Long vehicleId;
    private String vehicleRegistration;
    private String pickupLocation;
    private String dropLocation;
    private LocalDateTime scheduledPickupAt;
    private Double lastLatitude;
    private Double lastLongitude;
    private LocalDateTime lastLocationAt;
    private boolean signalLost;
}