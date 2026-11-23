package com.zynolo_nexus.meeting_room_booking_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomUtilizationReportExportDto {
    private String exportType;
    private LocalDateTime generatedDate;
    private LocalDate periodFrom;
    private LocalDate periodTo;
    private RoomUtilizationSummaryDto summary;
    private List<RoomUtilizationReportListItemDto> rows;
    private int totalRecords;
}
