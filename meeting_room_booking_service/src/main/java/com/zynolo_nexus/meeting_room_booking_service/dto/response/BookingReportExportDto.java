package com.zynolo_nexus.meeting_room_booking_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingReportExportDto {
    private String exportType;
    private LocalDateTime generatedDate;
    private BookingReportSummaryDto summary;
    private List<BookingReportListItemDto> rows;
    private int totalRecords;
}
