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
public class TripListItemDto {
    private Long id;
    private String tripNumber;
    private String requestNumber;
    private String requesterUsername;
    private String pickupLocation;
    private String dropLocation;
    private Integer passengerCount;
    private Long vehicleId;
    private String vehicleRegistration;
    private Long driverId;
    private String driverName;
    private LocalDateTime scheduledPickupAt;
    private String status;
    private String statusDescription;
    private LocalDateTime createdDate;
    private LocalDateTime lastModifiedDate;
    private String createdBy;
    private String lastModifiedBy;
}