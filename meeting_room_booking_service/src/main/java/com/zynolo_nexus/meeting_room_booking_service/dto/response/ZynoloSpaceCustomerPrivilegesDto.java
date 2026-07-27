package com.zynolo_nexus.meeting_room_booking_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ZynoloSpaceCustomerPrivilegesDto {
    private boolean add;
    private boolean update;
    private boolean view;
    private boolean search;
    private boolean activate;
    private boolean deactivate;
}
