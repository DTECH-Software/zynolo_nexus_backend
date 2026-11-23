package com.zynolo_nexus.po_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrackingTimelineEntryDto {
    private String stage;
    private String status;
    private String statusDescription;
    private LocalDateTime eventDate;
    private String performedBy;
    private String referenceNo;
    private String remark;
}
