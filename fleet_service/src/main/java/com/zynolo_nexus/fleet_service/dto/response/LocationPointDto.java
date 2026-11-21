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
public class LocationPointDto {
    private Double latitude;
    private Double longitude;
    private Double accuracyMeters;
    private Double speedKmh;
    private Double headingDegrees;
    private LocalDateTime recordedAt;
}