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
public class ZynoloSpaceCustomerFilterResultDto {
    private List<ZynoloSpaceCustomerListItemDto> content;
    private Integer size;
    private Integer totalRecords;
    private Integer page;
    private Integer totalPages;
}
