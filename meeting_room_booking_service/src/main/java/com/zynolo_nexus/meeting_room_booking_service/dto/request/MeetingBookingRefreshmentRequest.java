package com.zynolo_nexus.meeting_room_booking_service.dto.request;

import lombok.Data;

@Data
public class MeetingBookingRefreshmentRequest {
    private Long refreshmentId;
    private Long vendorId;
    private Integer quantity;
}
