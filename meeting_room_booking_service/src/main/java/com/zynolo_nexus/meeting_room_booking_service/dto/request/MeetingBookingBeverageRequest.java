package com.zynolo_nexus.meeting_room_booking_service.dto.request;

import lombok.Data;

@Data
public class MeetingBookingBeverageRequest {
    private Long beverageId;
    private Long vendorId;
    private Integer quantity;
}
