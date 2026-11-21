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
public class OutstandingDto {
    private String itemType;
    private String itemTypeDescription;
    private Long id;
    private String referenceNumber;
    private String relatedNumber;
    private Long transportRequestId;
    private Long tripId;
    private String requesterUsername;
    private String pickupLocation;
    private String dropLocation;
    private Integer passengerCount;
    private Long vehicleId;
    private String vehicleRegistration;
    private Long driverId;
    private String driverName;
    private LocalDateTime relevantAt;
    private LocalDateTime scheduledPickupAt;
    private LocalDateTime submittedAt;
    private LocalDateTime completedAt;
    private String status;
    private String statusDescription;
    private String notes;
}
