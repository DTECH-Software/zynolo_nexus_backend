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
public class OutstandingListItemDto {
    private String itemType;
    private String itemTypeDescription;
    private Long id;
    private String referenceNumber;
    private String relatedNumber;
    private String requesterUsername;
    private String pickupLocation;
    private String dropLocation;
    private LocalDateTime relevantAt;
    private String status;
    private String statusDescription;
}
