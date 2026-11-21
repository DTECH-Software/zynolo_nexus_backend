package com.zynolo_nexus.fleet_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TripFareDto {
    private Long id;
    private Long companyId;
    private String fareNumber;
    private Long tripId;
    private String tripNumber;
    private Long transportRequestId;
    private String requestNumber;
    private String requesterUsername;
    private String pickupLocation;
    private String dropLocation;
    private BigDecimal amount;
    private String currency;
    private String paymentMethod;
    private String paymentMethodDescription;
    private String notes;
    private LocalDateTime recordedAt;
    private String recordedBy;
    private String status;
    private String statusDescription;
    private LocalDateTime createdDate;
    private LocalDateTime lastModifiedDate;
    private String createdBy;
    private String lastModifiedBy;
}
