package com.zynolo_nexus.fleet_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TripDetailReferenceDataDto {
    private List<ReferenceOptionDto> defaultTripStatus;
    private Integer recommendedPollSeconds;
    private Integer signalLostAfterSeconds;
    private String websocketEndpoint;
    private String websocketTopicPattern;
    private TripDetailPrivilegesDto privileges;
}
