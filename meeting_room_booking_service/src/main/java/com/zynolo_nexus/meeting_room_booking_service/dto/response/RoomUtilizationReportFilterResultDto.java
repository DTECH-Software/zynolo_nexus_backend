package com.zynolo_nexus.meeting_room_booking_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomUtilizationReportFilterResultDto {
    private LocalDate periodFrom;
    private LocalDate periodTo;
    private RoomUtilizationSummaryDto summary;
    private List<RoomUtilizationReportListItemDto> content;
    private List<RoomUtilizationReportListItemDto> roomUtilizationChart;
    private List<RoomBookingDistributionDto> bookingDistributionChart;
    private int size;
    private long totalRecords;
    private int page;
    private int totalPages;
}
