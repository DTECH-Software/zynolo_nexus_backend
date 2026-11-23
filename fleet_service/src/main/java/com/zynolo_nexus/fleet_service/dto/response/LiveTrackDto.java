package com.zynolo_nexus.fleet_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LiveTrackDto {
    private Long tripId;
    private String tripNumber;
    private String status;
    private String statusDescription;
    private Long transportRequestId;
    private String requestNumber;
    private String requesterUsername;
    private String pickupLocation;
    private String dropLocation;
    private Long driverId;
    private String driverName;
    private Long vehicleId;
    private String vehicleRegistration;
    private LocalDateTime scheduledPickupAt;
    private LocalDateTime startedAt;
    private LocationPointDto lastLocation;
    private boolean signalLost;
    private Integer signalLostAfterSeconds;
    private List<LocationPointDto> trail;
}