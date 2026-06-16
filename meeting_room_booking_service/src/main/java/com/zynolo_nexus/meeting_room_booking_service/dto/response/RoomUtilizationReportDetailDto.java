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
public class RoomUtilizationReportDetailDto {
    private Long roomId;
    private String roomCode;
    private String roomName;
    private Integer capacity;
    private LocalDate periodFrom;
    private LocalDate periodTo;
    private RoomUtilizationReportListItemDto utilization;
    private List<RoomUtilizationBookingHistoryDto> bookingHistory;
}
