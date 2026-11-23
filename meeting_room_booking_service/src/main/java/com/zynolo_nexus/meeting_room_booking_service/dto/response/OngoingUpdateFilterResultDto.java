package com.zynolo_nexus.meeting_room_booking_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OngoingUpdateFilterResultDto {
    private List<OngoingUpdateListItemDto> content;
    private int size;
    private long totalRecords;
    private int page;
    private int totalPages;
}
