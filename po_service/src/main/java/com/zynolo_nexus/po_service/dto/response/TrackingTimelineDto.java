package com.zynolo_nexus.po_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrackingTimelineDto {
    private Long id;
    private String poNo;
    private String requestNo;
    private List<TrackingTimelineEntryDto> entries;
}
