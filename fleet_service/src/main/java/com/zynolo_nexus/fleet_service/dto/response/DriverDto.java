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
public class DriverDto {
    private Long id;
    private Long companyId;
    private String name;
    private String mobile;
    private String licenceNumber;
    private String linkedUsername;
    private Long usualVehicleId;
    private String usualVehicleRegistration;
    private String availability;
    private String availabilityDescription;
    private String notes;
    private String status;
    private String statusDescription;
    private LocalDateTime createdDate;
    private LocalDateTime lastModifiedDate;
    private String createdBy;
    private String lastModifiedBy;
}