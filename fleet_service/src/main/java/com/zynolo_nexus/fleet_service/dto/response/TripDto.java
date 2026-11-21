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
public class TripDto {
    private Long id;
    private Long companyId;
    private String tripNumber;
    private Long transportRequestId;
    private String requestNumber;
    private String requesterUsername;
    private String pickupLocation;
    private String dropLocation;
    private Integer passengerCount;
    private Long vehicleId;
    private String vehicleRegistration;
    private Long driverId;
    private String driverName;
    private String driverMobile;
    private LocalDateTime scheduledPickupAt;
    private LocalDateTime assignedAt;
    private String assignedBy;
    private String notes;
    private String status;
    private String statusDescription;
    private LocalDateTime createdDate;
    private LocalDateTime lastModifiedDate;
    private String createdBy;
    private String lastModifiedBy;
}