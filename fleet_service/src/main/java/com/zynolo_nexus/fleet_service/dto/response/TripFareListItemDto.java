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
public class TripFareListItemDto {
    private Long id;
    private String fareNumber;
    private Long tripId;
    private String tripNumber;
    private String requesterUsername;
    private BigDecimal amount;
    private String currency;
    private String paymentMethod;
    private String paymentMethodDescription;
    private LocalDateTime recordedAt;
    private String status;
    private String statusDescription;
    private LocalDateTime createdDate;
    private LocalDateTime lastModifiedDate;
    private String createdBy;
    private String lastModifiedBy;
}
