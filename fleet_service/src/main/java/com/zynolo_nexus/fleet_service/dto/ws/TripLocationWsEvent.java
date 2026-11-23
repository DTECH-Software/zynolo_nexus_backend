package com.zynolo_nexus.fleet_service.dto.ws;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TripLocationWsEvent {
    private Long tripId;
    private String tripNumber;
    private Long companyId;
    private Double latitude;
    private Double longitude;
    private Double accuracyMeters;
    private Double speedKmh;
    private Double headingDegrees;
    private LocalDateTime recordedAt;
    private LocalDateTime receivedAt;
    private String eventType;
}