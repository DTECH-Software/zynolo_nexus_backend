package com.zynolo_nexus.meeting_room_booking_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CalendarPrivilegesDto {
    private boolean view;
    private boolean search;
    private boolean createBooking;
    private boolean cancelBooking;
    private boolean myBookings;
}
