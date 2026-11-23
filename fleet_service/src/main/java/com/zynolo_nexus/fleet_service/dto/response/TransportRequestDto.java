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
public class TransportRequestDto {
    private Long id;
    private Long companyId;
    private String requestNumber;
    private String requesterUsername;
    private String pickupLocation;
    private String dropLocation;
    private LocalDateTime requestedPickupAt;
    private Integer passengerCount;
    private String contactMobile;
    private String purpose;
    private String notes;
    private String dispatcherNotes;
    private String status;
    private String statusDescription;
    private LocalDateTime submittedAt;
    private LocalDateTime createdDate;
    private LocalDateTime lastModifiedDate;
    private String createdBy;
    private String lastModifiedBy;
}